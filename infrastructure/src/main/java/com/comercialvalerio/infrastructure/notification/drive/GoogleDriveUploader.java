package com.comercialvalerio.infrastructure.notification.drive;
import com.comercialvalerio.domain.exception.NotificationException;
import com.comercialvalerio.infrastructure.config.AppConfig;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.File;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PreDestroy;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class GoogleDriveUploader {
    private static final Logger LOG =
        Logger.getLogger(GoogleDriveUploader.class.getName());
    private static final AtomicReference<Drive> drive = new AtomicReference<>();
    private static final AtomicReference<NetHttpTransport> transport =
        new AtomicReference<>();

    private static Drive drive() {
        Drive svc = drive.get();
        if (svc == null) {
            Drive candidate = createDriveService();
            if (drive.compareAndSet(null, candidate)) {
                svc = candidate;
            } else {
                svc = drive.get();
            }
        }
        return svc;
    }

    private static GoogleClientSecrets readCredentials(String path)
            throws IOException {
        java.io.InputStream in;
        if (Files.exists(Paths.get(path))) {
            in = Files.newInputStream(Paths.get(path));
        } else {
            in = loadFromClasspath(path);
        }
        try (var reader = new java.io.InputStreamReader(
                     in, java.nio.charset.StandardCharsets.UTF_8)) {
            return GoogleClientSecrets.load(
                    GsonFactory.getDefaultInstance(),
                    reader);
        }
    }

    private static AuthorizationCodeInstalledApp buildAuthorizationFlow(
            GoogleClientSecrets secrets, List<String> scopes, String tokenDir)
            throws IOException, GeneralSecurityException {
        NetHttpTransport httpTransport =
                GoogleNetHttpTransport.newTrustedTransport();
        transport.set(httpTransport);

        var flow = new com.google.api.client.googleapis.auth.oauth2
                .GoogleAuthorizationCodeFlow.Builder(
                        httpTransport, GsonFactory.getDefaultInstance(),
                        secrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(
                        Paths.get(tokenDir).toFile()))
                .setAccessType("offline")
                .build();

        int port = AppConfig.getInt("drive.localPort");
        var receiver = new LocalServerReceiver.Builder()
                              .setPort(port).build();
        return new AuthorizationCodeInstalledApp(flow, receiver);
    }

    private static void cleanCorruptedTokens(String tokenDir) {
        try {
            java.nio.file.Path dir = java.nio.file.Paths.get(tokenDir);
            if (java.nio.file.Files.exists(dir)) {
                java.nio.file.Files.walk(dir)
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(java.nio.file.Path::toFile)
                        .forEach(java.io.File::delete);
            }
        } catch (IOException delEx) {
            LOG.log(Level.WARNING,
                    "No se pudo limpiar el directorio de tokens", delEx);
        }
    }

    private static Drive createDriveService() {
        String tokenDir  = AppConfig.get("drive.tokensDir");
        try {
            String credPath  = AppConfig.get("drive.credentialsPath");
            List<String> scopes = Arrays.asList(
                AppConfig.getList("drive.scopes"));

            GoogleClientSecrets secrets = readCredentials(credPath);
            var auth = buildAuthorizationFlow(secrets, scopes, tokenDir);
            NetHttpTransport httpTransport = transport.get();

            Drive svc = new Drive.Builder(httpTransport,
                    GsonFactory.getDefaultInstance(),
                    auth.authorize("user"))
                   .setApplicationName(
                    AppConfig.get("drive.appName"))
                   .build();
            return svc;

        } catch (java.io.EOFException eof) {
            LOG.log(Level.WARNING,
                    "Tokens de Drive dañados, se eliminarán", eof);
            cleanCorruptedTokens(tokenDir);
            return createDriveService();

        } catch (IOException | GeneralSecurityException ex) {
            LOG.log(Level.SEVERE,
                    "No se pudo inicializar el servicio de almacenamiento",
                    ex);
            throw new NotificationException(
                "No se pudo inicializar el servicio de almacenamiento",
                ex);
        }
    }
    /* Sube el PDF y devuelve la URL de descarga pública. */
    public String uploadAndPublicLink(java.io.File localFile) {
        try {
            Drive svc = drive();
            File meta = new File().setName(localFile.getName());
            FileContent content = new FileContent("application/pdf", localFile);
            File up = svc.files()
                           .create(meta, content)
                           .setFields("id")
                           .execute();

            // permiso “anyone reader”
            svc.permissions()
                 .create(up.getId(),
                         new Permission()
                           .setType("anyone")
                           .setRole("reader"))
                 .execute();

            String url = "https://drive.google.com/uc?id="
                         + up.getId()
                         + "&export=download";
            LOG.log(Level.INFO,
                    "Documento subido a Drive con id {0}", up.getId());
            return url;

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error al subir el documento", ex);
            throw new NotificationException(
                "Error al subir el documento", ex);
        } finally {
            try {
                Files.deleteIfExists(localFile.toPath());
            } catch (IOException delEx) {
                LOG.log(Level.WARNING,
                        "No se pudo borrar el archivo local "
                        + localFile.getAbsolutePath(),
                        delEx);
            }
        }
    }

    /*
     * Carga un recurso desde el classpath usando primero el class loader de la
     * hebra actual y luego el de esta clase.
     */
    private static java.io.InputStream loadFromClasspath(String resource)
            throws IOException {
        ClassLoader ctx = Thread.currentThread().getContextClassLoader();
        java.io.InputStream in = null;
        if (ctx != null) {
            in = ctx.getResourceAsStream(resource);
        }
        if (in == null) {
            in = GoogleDriveUploader.class.getClassLoader()
                     .getResourceAsStream(resource);
        }
        if (in == null) {
            throw new IOException("No se encontró " + resource + " en el classpath");
        }
        return in;
    }

    @PreDestroy
    void shutdown() {
        NetHttpTransport t = transport.getAndSet(null);
        if (t != null) {
            try {
                t.shutdown();
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Error al cerrar el transporte de Drive", ex);
            }
        }
    }
}

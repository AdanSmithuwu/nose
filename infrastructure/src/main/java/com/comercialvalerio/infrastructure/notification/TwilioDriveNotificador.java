package com.comercialvalerio.infrastructure.notification;
import com.comercialvalerio.domain.notification.NotificadorComprobante;
import com.comercialvalerio.domain.exception.NotificationException;
import com.comercialvalerio.infrastructure.notification.drive.GoogleDriveUploader;
import com.comercialvalerio.infrastructure.notification.whatsapp.TwilioWhatsAppSender;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class TwilioDriveNotificador implements NotificadorComprobante {
    private static final Logger LOG =
        Logger.getLogger(TwilioDriveNotificador.class.getName());
    private final GoogleDriveUploader   driveUploader;
    private final TwilioWhatsAppSender whatsappSender;

    @Inject
    public TwilioDriveNotificador(GoogleDriveUploader driveUploader,
                                  TwilioWhatsAppSender whatsappSender) {
        this.driveUploader  = driveUploader;
        this.whatsappSender = whatsappSender;
    }
    @Override
    public void notificar(File pdf, String telefonoCliente) throws NotificationException {
        try {
            // 1) subir a Drive
            String url = driveUploader.uploadAndPublicLink(pdf);
            LOG.log(Level.INFO, "PDF subido, URL: {0}", url);
            // 2) enviar WhatsApp
            whatsappSender.sendTemplate(telefonoCliente, url);
            LOG.log(Level.INFO, "Notificación enviada a {0}", telefonoCliente);
        } catch (NotificationException ex) {
            LOG.log(Level.SEVERE, "Error enviando comprobante", ex);
            throw ex;
        }
    }
}

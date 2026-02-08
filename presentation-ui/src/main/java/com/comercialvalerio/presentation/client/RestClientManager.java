package com.comercialvalerio.presentation.client;

import com.comercialvalerio.presentation.config.UiConfig;
import com.comercialvalerio.common.json.JsonbResolver;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona el ciclo de vida del {@link Client} HTTP usado por la capa de UI.
 */
public final class RestClientManager {
    private static String baseUrl;
    private static Client client;
    private static final Logger LOG = Logger.getLogger(RestClientManager.class.getName());

    private RestClientManager() {}

    /**
     * Inicializa el cliente REST usando la configuración de {@link UiConfig}.
     * Si existe un cliente previo se cerrará antes de crear uno nuevo.
     */
    public static void init() {
        if (client != null) {
            // asegurar que los recursos del cliente previo se liberen
            close();
        }
        baseUrl = UiConfig.getApiBaseUrl().strip();
        long connectMs = UiConfig.getConnectTimeout();
        long readMs = UiConfig.getReadTimeout();
        LOG.log(Level.INFO, "REST client base URL: {0}", baseUrl);
        client = ClientBuilder.newBuilder()
                .connectTimeout(connectMs, TimeUnit.MILLISECONDS)
                .readTimeout(readMs, TimeUnit.MILLISECONDS)
                .build();
        client.register(JsonbResolver.class);
        if (UiConfig.isLogBitacoraResponses()) {
            client.register(BitacoraResponseLogFilter.class);
        }
        client.register(new AuthHeaderFilter());

        if (LOG.isLoggable(Level.FINE)) {
            java.util.ServiceLoader<jakarta.json.bind.spi.JsonbProvider> sl =
                    java.util.ServiceLoader.load(jakarta.json.bind.spi.JsonbProvider.class);
            java.util.List<String> providers = new java.util.ArrayList<>();
            for (jakarta.json.bind.spi.JsonbProvider p : sl) {
                providers.add(p.getClass().getName());
            }
            LOG.log(Level.FINE, "Jsonb providers: {0}", providers);
        }
    }

    /** Cierra el cliente REST subyacente. */
    public static void close() {
        if (client != null) {
            client.close();
            client = null;
            baseUrl = null;
            LOG.fine("REST client closed");
        }
    }

    public static String baseUrl() {
        return baseUrl;
    }

    public static Client client() {
        return client;
    }
}

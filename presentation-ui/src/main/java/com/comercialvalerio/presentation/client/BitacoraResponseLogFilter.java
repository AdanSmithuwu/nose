package com.comercialvalerio.presentation.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;

/**
 * Registra el cuerpo completo de las respuestas a peticiones de "/bitacoras".
 * Solo debe activarse con fines de depuración.
 */
public class BitacoraResponseLogFilter implements ClientResponseFilter {
    private static final Logger LOG = Logger.getLogger(BitacoraResponseLogFilter.class.getName());

    @Override
    public void filter(ClientRequestContext req, ClientResponseContext res) throws IOException {
        String path = req.getUri().getPath();
        if (path != null && path.contains("/bitacoras") && res.hasEntity()) {
            byte[] bytes = res.getEntityStream().readAllBytes();
            String body = new String(bytes, StandardCharsets.UTF_8);
            LOG.log(Level.FINE, "Respuesta de /bitacoras: {0}", body);
            res.setEntityStream(new ByteArrayInputStream(bytes));
        }
    }
}

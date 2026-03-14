package com.comercialvalerio.application.rest.exception;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comercialvalerio.domain.exception.NotificationException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class NotificationExceptionMapper implements ExceptionMapper<NotificationException> {
    private static final Logger LOG = Logger.getLogger(NotificationExceptionMapper.class.getName());

    @Override
    public Response toResponse(NotificationException ex) {
        LOG.log(Level.SEVERE, "Notification error", ex);
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "Error al enviar la notificación";
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity(Map.of("error", msg))
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}

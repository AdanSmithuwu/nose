package com.comercialvalerio.application.rest.exception;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ResteasyViolationException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ResteasyViolationExceptionMapper implements ExceptionMapper<ResteasyViolationException> {
    private static final Logger LOG = Logger.getLogger(ResteasyViolationExceptionMapper.class.getName());

    @Override
    public Response toResponse(ResteasyViolationException ex) {
        List<String> mensajes = Stream
                .concat(ex.getParameterViolations().stream(),
                        ex.getPropertyViolations().stream())
                .map(ResteasyConstraintViolation::getMessage)
                .toList();
        String mensaje = String.join("; ", mensajes);
        // Registramos solo el mensaje para evitar trazas extensas en el log
        LOG.log(Level.WARNING, "Validation failure: {0}", mensaje);
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity(Map.of("error", mensaje))
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}

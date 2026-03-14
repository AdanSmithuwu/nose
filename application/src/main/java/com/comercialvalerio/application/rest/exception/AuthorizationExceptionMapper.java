package com.comercialvalerio.application.rest.exception;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comercialvalerio.domain.exception.AuthorizationException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class AuthorizationExceptionMapper implements ExceptionMapper<AuthorizationException> {
    private static final Logger LOG = Logger.getLogger(AuthorizationExceptionMapper.class.getName());
    @Override
    public Response toResponse(AuthorizationException ex) {
        LOG.log(Level.SEVERE, "Authorization failure", ex);
        return Response.status(Response.Status.FORBIDDEN)
                       .entity(Map.of("error", ex.getMessage()))
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}

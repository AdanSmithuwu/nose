package com.comercialvalerio.application.rest.exception;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comercialvalerio.domain.exception.EntityNotFoundException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Mapea EntityNotFoundException a HTTP 404. */
@Provider
@ApplicationScoped
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {
    private static final Logger LOG = Logger.getLogger(EntityNotFoundExceptionMapper.class.getName());
    @Override
    public Response toResponse(EntityNotFoundException ex) {
        LOG.log(Level.SEVERE, "Entity not found", ex);
        return Response.status(Response.Status.NOT_FOUND)
                       .entity(Map.of("error", ex.getMessage()))
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}

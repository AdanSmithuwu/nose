package com.comercialvalerio.application.rest.exception;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comercialvalerio.common.exception.ConfigException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class ConfigExceptionMapper implements ExceptionMapper<ConfigException> {
    private static final Logger LOG = Logger.getLogger(ConfigExceptionMapper.class.getName());

    @Override
    public Response toResponse(ConfigException ex) {
        LOG.log(Level.SEVERE, "Configuration error", ex);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity(Map.of("error", "Error de configuración del sistema"))
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}

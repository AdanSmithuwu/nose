package com.comercialvalerio.application.rest.exception;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comercialvalerio.domain.exception.BusinessRuleViolationException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class BusinessRuleViolationExceptionMapper implements ExceptionMapper<BusinessRuleViolationException> {
    private static final Logger LOG = Logger.getLogger(BusinessRuleViolationExceptionMapper.class.getName());
    @Override
    public Response toResponse(BusinessRuleViolationException ex) {
        LOG.log(Level.SEVERE, "Business rule violation", ex);
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity(Map.of("error", ex.getMessage()))
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}

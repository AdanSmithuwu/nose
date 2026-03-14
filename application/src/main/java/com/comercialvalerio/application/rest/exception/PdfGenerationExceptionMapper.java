package com.comercialvalerio.application.rest.exception;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comercialvalerio.application.exception.PdfGenerationException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class PdfGenerationExceptionMapper implements ExceptionMapper<PdfGenerationException> {
    private static final Logger LOG = Logger.getLogger(PdfGenerationExceptionMapper.class.getName());
    @Override
    public Response toResponse(PdfGenerationException ex) {
        LOG.log(Level.SEVERE, "PDF generation error", ex);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity(Map.of("error", ex.getMessage()))
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}

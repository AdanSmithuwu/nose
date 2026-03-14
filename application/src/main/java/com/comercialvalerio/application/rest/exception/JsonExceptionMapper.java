package com.comercialvalerio.application.rest.exception;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comercialvalerio.domain.exception.DataAccessException;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.exception.DuplicateKeyException;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JsonExceptionMapper implements ExceptionMapper<Throwable> {
  private static final Logger LOG = Logger.getLogger(JsonExceptionMapper.class.getName());
  @Override
  public Response toResponse(Throwable ex) {
    if (ex instanceof ConstraintViolationException cve) {
      LOG.log(Level.SEVERE, "Validation error", cve);
      String msg = cve.getConstraintViolations().stream()
                     .map(v -> v.getMessage())
                     .reduce((a,b) -> a + "; " + b)
                     .orElse("Datos inválidos");
      return Response.status(Status.BAD_REQUEST)
                     .entity(Map.of("error", msg))
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    if (ex instanceof BadRequestException) {
      LOG.log(Level.SEVERE, "Bad request", ex);
      LOG.log(Level.FINE, ex.getMessage());
      return Response.status(Status.BAD_REQUEST)
                     .entity(Map.of("error", "Solicitud inválida"))
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    if (ex instanceof IllegalStateException || ex instanceof IllegalArgumentException) {
      LOG.log(Level.SEVERE, "Request error", ex);
      return Response.status(Status.BAD_REQUEST)
                     .entity(Map.of("error", ex.getMessage()))
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    if (ex instanceof DuplicateEntityException || ex instanceof DuplicateKeyException) {
      LOG.log(Level.SEVERE, "Conflict", ex);
      String msg = ex.getMessage();
      if (msg == null || msg.isBlank()) {
        msg = "Duplicidad en BD";
      }
      return Response.status(Status.CONFLICT)
                     .entity(Map.of("error", msg))
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    if (ex instanceof DataAccessException) {
      LOG.log(Level.SEVERE, "Data access error", ex);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity(Map.of("error", "Error de acceso a datos"))
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    // valor por defecto 500
    LOG.log(Level.SEVERE, "Unhandled exception", ex);
    return Response.status(Status.INTERNAL_SERVER_ERROR)
                   .entity(Map.of("error", "Error inesperado"))
                   .type(MediaType.APPLICATION_JSON)
                   .build();
  }
}

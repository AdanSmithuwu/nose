package com.comercialvalerio.application.rest.exception;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comercialvalerio.domain.exception.DataAccessException;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.exception.DuplicateKeyException;
import com.comercialvalerio.domain.exception.ConstraintViolationException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class DataAccessExceptionMapper implements ExceptionMapper<DataAccessException> {
  private static final Logger LOG = Logger.getLogger(DataAccessExceptionMapper.class.getName());
  @Override
  public Response toResponse(DataAccessException ex) {
    LOG.log(Level.SEVERE, "Data access failure", ex);
    if (ex instanceof ConstraintViolationException) {
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity(Map.of("error", "Datos inválidos"))
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    if (ex instanceof DuplicateEntityException || ex instanceof DuplicateKeyException) {
      String msg = ex.getMessage();
      if (msg == null || msg.isBlank()) {
        msg = "Duplicidad en BD";
      }
      return Response.status(Response.Status.CONFLICT)
                     .entity(Map.of("error", msg))
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    return Response
        .status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(Map.of("error", "Error de acceso a datos"))
        .type(MediaType.APPLICATION_JSON)
        .build();
  }
}

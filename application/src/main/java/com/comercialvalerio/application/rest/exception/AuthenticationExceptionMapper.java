package com.comercialvalerio.application.rest.exception;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comercialvalerio.domain.exception.AuthenticationException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class AuthenticationExceptionMapper
     implements ExceptionMapper<AuthenticationException> {
  private static final Logger LOG = Logger.getLogger(AuthenticationExceptionMapper.class.getName());
  @Override
  public Response toResponse(AuthenticationException ex) {
    LOG.log(Level.SEVERE, "Authentication failed", ex);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", ex.getMessage());
    if (ex.getMinutosRestantes() != null) {
      body.put("detalle", ex.getMinutosRestantes());
    }
    return Response.status(Response.Status.UNAUTHORIZED)
                   .entity(body)
                   .type(MediaType.APPLICATION_JSON)
                   .build();
  }
}

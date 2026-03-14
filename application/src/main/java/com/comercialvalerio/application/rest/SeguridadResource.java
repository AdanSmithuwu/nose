package com.comercialvalerio.application.rest;

import com.comercialvalerio.domain.security.service.AutenticacionService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import com.comercialvalerio.application.rest.util.Responses;

@Path("/seguridad")
@RequestScoped
public class SeguridadResource implements SeguridadResourceApi {

    @Inject
    AutenticacionService authSvc;

    @POST
    @Path("/refrescar-limites")
    @Override
    public Response refrescarLimites() {
        authSvc.refrescarLimites();
        return Responses.noContent();
    }
}

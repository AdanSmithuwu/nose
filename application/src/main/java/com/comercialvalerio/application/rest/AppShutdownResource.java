package com.comercialvalerio.application.rest;

import com.comercialvalerio.application.service.AppShutdownService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/** Recurso REST para operaciones de ciclo de vida de la aplicación. */
@Path("/lifecycle")
@RequestScoped
public class AppShutdownResource extends BaseResource implements AppShutdownResourceApi {

    @Inject
    AppShutdownService svc;

    @POST
    @Path("/shutdown")
    @Override
    public Response shutdown() {
        svc.shutdown();
        return noContent();
    }
}

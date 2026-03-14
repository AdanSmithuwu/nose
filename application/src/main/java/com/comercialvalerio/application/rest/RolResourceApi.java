package com.comercialvalerio.application.rest;
import com.comercialvalerio.application.dto.RolDto;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.validation.constraints.NotNull;

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface RolResourceApi {
    @Context
    @GET
    public Response listar(@QueryParam("nombre") String nombre);

    /** Sobrecarga de conveniencia para proxies de UI. */
    default Response listar() {
        return listar(null);
    }

    /** Envoltura de conveniencia usada por {@link com.comercialvalerio.application.service.RolService#buscarPorNombre}. */
    default Response buscarPorNombre(String nombre) {
        return listar(nombre);
    }
    @GET
    @Path("{id}")
    public RolDto obtener(@PathParam("id") @NotNull Integer id);
}

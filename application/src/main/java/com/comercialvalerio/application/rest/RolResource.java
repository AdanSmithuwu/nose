package com.comercialvalerio.application.rest;
import java.util.List;

import com.comercialvalerio.application.dto.RolDto;
import com.comercialvalerio.application.service.RolService;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RolResource extends BaseResource implements RolResourceApi {

    @Inject
    private RolService svc;

    @GET
    @Override
    public Response listar(@QueryParam("nombre") String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            RolDto rol = svc.buscarPorNombre(nombre);
            return ok(rol);
        } else {
            List<RolDto> todos = svc.listar();
            return ok(todos);
        }
    }

    @GET
    @Path("{id}")
    @Override
    public RolDto obtener(@PathParam("id") @NotNull Integer id) {
        return svc.obtener(id);
    }

}

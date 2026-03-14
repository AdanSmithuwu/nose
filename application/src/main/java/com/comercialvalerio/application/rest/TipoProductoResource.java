package com.comercialvalerio.application.rest;
import java.net.URI;
import java.util.List;

import com.comercialvalerio.application.dto.TipoProductoCreateDto;
import com.comercialvalerio.application.dto.TipoProductoDto;
import com.comercialvalerio.application.service.TipoProductoService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.comercialvalerio.application.rest.BaseResource;

import jakarta.ws.rs.core.UriInfo;

@Path("/tipos-producto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TipoProductoResource extends BaseResource implements TipoProductoResourceApi {

    @Inject
    private TipoProductoService svc;

    @Context
    private UriInfo uriInfo;

    /* Listar todos o buscar por nombre: /tipos-producto?nombre=xxx */
    @GET
    @Override
    public Response listar(
        @QueryParam("nombre") String nombre
    ) {
        if (nombre != null && !nombre.isBlank()) {
            TipoProductoDto one = svc.buscarPorNombre(nombre);
            return ok(one);
        } else {
            List<TipoProductoDto> all = svc.listar();
            return ok(all);
        }
    }

    @GET
    @Path("{id}")
    @Override
    public TipoProductoDto obtener(@PathParam("id") @NotNull Integer id) {
        return svc.obtener(id);
    }

    @POST
    @Override
    public Response crear(
        @Valid TipoProductoCreateDto dto
    ) {
        TipoProductoDto created = svc.crear(dto);
        URI uri = uriInfo.getAbsolutePathBuilder()
                         .path(created.idTipoProducto().toString())
                         .build();
        return created(uri, created);
    }

    @PUT
    @Path("{id}")
    @Override
    public TipoProductoDto actualizar(
        @PathParam("id") @NotNull Integer id,
        @Valid TipoProductoCreateDto dto
    ) {
        return svc.actualizar(id, dto);
    }

    @DELETE
    @Path("{id}")
    @Override
    public Response eliminar(@PathParam("id") @NotNull Integer id) {
        svc.eliminar(id);
        return noContent();
    }
}

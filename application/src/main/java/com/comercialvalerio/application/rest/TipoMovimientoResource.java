package com.comercialvalerio.application.rest;
import java.net.URI;
import java.util.List;

import com.comercialvalerio.application.dto.TipoMovimientoCreateDto;
import com.comercialvalerio.application.dto.TipoMovimientoDto;
import com.comercialvalerio.application.service.TipoMovimientoService;

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
import jakarta.ws.rs.core.UriInfo;

@Path("/tipos-mov")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoMovimientoResource extends BaseResource implements TipoMovimientoResourceApi {

    @Inject
    private TipoMovimientoService svc;

    @Context
    private UriInfo uriInfo;

    /*
     * GET /tipos-mov            → lista completa
     * GET /tipos-mov?nombre=X   → búsqueda por nombre
     */
    @GET
    @Override
    public Response listar(
        @QueryParam("nombre") String nombre
    ) {
        if (nombre != null && !nombre.isBlank()) {
            TipoMovimientoDto one = svc.buscarPorNombre(nombre);
            return ok(one);
        } else {
            List<TipoMovimientoDto> all = svc.listar();
            return ok(all);
        }
    }

    @GET @Path("{id}")
    @Override
    public Response obtener(@PathParam("id") @NotNull Integer id) {
        TipoMovimientoDto dto = svc.obtener(id);
        return ok(dto);
    }

    @POST
    @Override
    public Response crear(
        @Valid TipoMovimientoCreateDto dto
    ) {
        TipoMovimientoDto created = svc.crear(dto);
        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(created.idTipoMovimiento().toString())
                              .build();
        return created(location, created);
    }

    @PUT @Path("{id}")
    @Override
    public Response actualizar(
        @PathParam("id") @NotNull Integer id,
        @Valid TipoMovimientoCreateDto dto
    ) {
        TipoMovimientoDto updated = svc.actualizar(id, dto);
        return ok(updated);
    }

    @DELETE @Path("{id}")
    @Override
    public Response eliminar(@PathParam("id") @NotNull Integer id) {
        svc.eliminar(id);
        return noContent();
    }
}

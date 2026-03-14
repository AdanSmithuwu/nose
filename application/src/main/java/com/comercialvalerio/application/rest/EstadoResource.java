package com.comercialvalerio.application.rest;
import java.net.URI;
import java.util.List;

import com.comercialvalerio.application.dto.EstadoCreateDto;
import com.comercialvalerio.application.dto.EstadoDto;
import com.comercialvalerio.application.service.EstadoService;

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

@Path("/estados")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EstadoResource extends BaseResource implements EstadoResourceApi {

    @Inject
    private EstadoService svc;

    @Context
    private UriInfo uriInfo;

    @GET
    @Override
    public Response listar(
          @QueryParam("modulo") String modulo,
          @QueryParam("nombre") String nombre
    ) {
        if (modulo != null && !modulo.isBlank()
         && nombre != null && !nombre.isBlank()) {
            EstadoDto dto = svc.buscarPorModuloYNombre(modulo, nombre);
            return ok(dto);
        } else {
            List<EstadoDto> all = svc.listar();
            return ok(all);
        }
    }

    @GET
    @Path("{id}")
    @Override
    public EstadoDto obtener(@PathParam("id") @NotNull Integer id) {
        return svc.obtener(id);
    }

    @POST
    @Override
    public Response crear(@Valid EstadoCreateDto dto) {
        EstadoDto created = svc.crear(dto);
        URI uri = uriInfo.getAbsolutePathBuilder()
                         .path(created.idEstado().toString())
                         .build();
        return created(uri, created);
    }

    @PUT
    @Path("{id}")
    @Override
    public EstadoDto actualizar(@PathParam("id") @NotNull Integer id,
                                @Valid EstadoCreateDto dto) {
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

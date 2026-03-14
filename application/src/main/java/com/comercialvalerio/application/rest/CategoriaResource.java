package com.comercialvalerio.application.rest;
import java.net.URI;
import java.util.List;

import com.comercialvalerio.application.dto.CategoriaCreateDto;
import com.comercialvalerio.application.dto.CategoriaDto;
import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.service.CategoriaService;

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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import com.comercialvalerio.application.rest.BaseResource;
import jakarta.annotation.security.RolesAllowed;

@Path("/categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaResource extends BaseResource implements CategoriaResourceApi {

    @Inject
    CategoriaService svc;

    @Context
    UriInfo uriInfo;

    @GET
    @Override
    public List<CategoriaDto> listar() {
        return svc.listar();
    }

    @GET @Path("{id}")
    @Override
    public CategoriaDto obtener(@PathParam("id") @NotNull Integer id) {
        return svc.obtener(id);
    }

    @POST
    @RolesAllowed("Administrador")
    @Override
    public Response crear(@Valid CategoriaCreateDto dto) {
        CategoriaDto created = svc.crear(dto);
        URI uri = uriInfo.getAbsolutePathBuilder()
                         .path(created.idCategoria().toString())
                         .build();
        return created(uri, created);
    }

    @PUT @Path("{id}")
    @RolesAllowed("Administrador")
    @Override
    public CategoriaDto actualizar(@PathParam("id") @NotNull Integer id,
                               @Valid CategoriaCreateDto dto) {
        return svc.actualizar(id, dto);
    }

    @DELETE @Path("{id}")
    @Override
    public Response eliminar(@PathParam("id") @NotNull Integer id) {
        svc.eliminar(id);
        return noContent();
    }

    @PUT @Path("{id}/estado")
    @RolesAllowed("Administrador")
    @Override
    public Response cambiarEstado(@PathParam("id") @NotNull Integer id,
                                  @Valid CambiarEstadoDto dto,
                                  @jakarta.ws.rs.QueryParam("actualizarProductos")
                                  @jakarta.ws.rs.DefaultValue("true") boolean actualizarProductos) {
        svc.cambiarEstado(id, dto, actualizarProductos);
        return noContent();
    }

    @GET @Path("{id}/eliminable")
    @Override
    public List<String> obtenerDependencias(@PathParam("id") @NotNull Integer id) {
        return svc.obtenerDependencias(id);
    }
}

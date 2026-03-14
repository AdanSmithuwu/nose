package com.comercialvalerio.application.rest;
import java.net.URI;
import java.util.List;

import com.comercialvalerio.application.dto.PresentacionCreateDto;
import com.comercialvalerio.application.dto.PresentacionDto;
import com.comercialvalerio.application.service.PresentacionService;

import jakarta.enterprise.context.RequestScoped;
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

// ----------  PRESENTACIONES  -----------------------------------------
@Path("/presentaciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class PresentacionResource extends BaseResource implements PresentacionResourceApi {

    @Inject PresentacionService svc;
    @Context UriInfo uri;

    @GET
    @Override
    public List<PresentacionDto> listarPorProducto(
            @QueryParam("producto") Integer idProd){
        return (idProd == null)
               ? List.of()
               : svc.listarPorProducto(idProd);
    }

    @GET @Path("{id}")
    @Override
    public PresentacionDto obtener(@PathParam("id") @NotNull Integer id){
        return svc.obtener(id);
    }

    @POST
    @Override
    public Response crear(@Valid PresentacionCreateDto in){
        PresentacionDto dto = svc.crear(in);
        URI location = uri.getAbsolutePathBuilder()
                          .path(dto.idPresentacion().toString())
                          .build();
        return created(location, dto);
    }

    @PUT @Path("{id}")
    @Override
    public PresentacionDto actualizar(@PathParam("id") @NotNull Integer id,
                                  @Valid PresentacionCreateDto in){
        return svc.actualizar(id, in);
    }

    @DELETE @Path("{id}")
    @Override
    public Response eliminar(@PathParam("id") @NotNull Integer id){
        svc.eliminar(id);
        return noContent();
    }

    @PUT @Path("{id}/activar")
    @Override
    public Response activar(@PathParam("id") @NotNull Integer id){
        svc.activar(id);
        return noContent();
    }

    @PUT @Path("{id}/desactivar")
    @Override
    public Response desactivar(@PathParam("id") @NotNull Integer id){
        svc.desactivar(id);
        return noContent();
    }

    @GET @Path("all")
    @Override
    public List<PresentacionDto> listarTodosPorProducto(
            @QueryParam("producto") Integer idProd){
        return idProd == null ? List.of() : svc.listarTodosPorProducto(idProd);
    }
}

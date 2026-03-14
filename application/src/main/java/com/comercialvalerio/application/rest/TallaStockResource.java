package com.comercialvalerio.application.rest;
import java.net.URI;
import java.util.List;

import com.comercialvalerio.application.dto.TallaStockCreateDto;
import com.comercialvalerio.application.dto.TallaStockDto;
import com.comercialvalerio.application.service.TallaStockService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
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

// ----------  TALLAS  --------------------------------------------------
@Path("/tallas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class TallaStockResource extends BaseResource implements TallaStockResourceApi {

    @Inject TallaStockService svc;
    @Context UriInfo uri;

    /* 1) Listado o filtro por ?producto= */
    @GET
    @Override
    public List<TallaStockDto> listarPorProducto(
            @QueryParam("producto") Integer idProd){
        return (idProd == null)
               ? List.of()                // no se permite listado global
               : svc.listarPorProducto(idProd);
    }

    @GET @Path("{id}")
    @Override
    public TallaStockDto obtener(@PathParam("id") @NotNull Integer id){
        return svc.obtener(id);
    }

    @POST
    @Override
    public Response crear(@Valid TallaStockCreateDto in){
        TallaStockDto dto = svc.crear(in);
        URI location = uri.getAbsolutePathBuilder()
                          .path(dto.idTallaStock().toString())
                          .build();
        return created(location, dto);
    }

    @PUT @Path("{id}")
    @Override
    public TallaStockDto actualizar(@PathParam("id") @NotNull Integer id,
                                @Valid TallaStockCreateDto in){
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

    @GET @Path("{id}/eliminable")
    @Override
    public List<String> obtenerDependencias(@PathParam("id") @NotNull Integer id) {
        return svc.obtenerDependencias(id);
    }

    @GET @Path("all")
    @Override
    public List<TallaStockDto> listarTodosPorProducto(
            @QueryParam("producto") Integer idProd){
        return idProd == null ? List.of() : svc.listarTodosPorProducto(idProd);
    }

    /* PATCH semántico para ajustar stock */
    @PATCH @Path("{id}")
    @RolesAllowed("Administrador")
    @Override
    public Response ajustarStock(@PathParam("id") @NotNull Integer id,
                                 TallaStockResourceApi.AjusteStockDto body){
        svc.ajustarStock(id, body.delta);
        return noContent();
}
}

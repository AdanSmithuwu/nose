package com.comercialvalerio.application.rest;
import java.util.List;

import com.comercialvalerio.application.dto.PresentacionCreateDto;
import com.comercialvalerio.application.dto.PresentacionDto;

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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/presentaciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PresentacionResourceApi {
    @GET
    public List<PresentacionDto> listarPorProducto( @QueryParam("producto") Integer idProd);
    @GET @Path("{id}")
    public PresentacionDto obtener(@PathParam("id") @NotNull Integer id);
    @POST
    public Response crear(@Valid PresentacionCreateDto in);
    @PUT @Path("{id}")
    public PresentacionDto actualizar(@PathParam("id") @NotNull Integer id, @Valid PresentacionCreateDto in);
    @DELETE @Path("{id}")
    public Response eliminar(@PathParam("id") @NotNull Integer id);
    @PUT @Path("{id}/activar")
    public Response activar(@PathParam("id") @NotNull Integer id);
    @PUT @Path("{id}/desactivar")
    public Response desactivar(@PathParam("id") @NotNull Integer id);
    @GET @Path("all")
    public List<PresentacionDto> listarTodosPorProducto(
            @QueryParam("producto") Integer idProd);
}

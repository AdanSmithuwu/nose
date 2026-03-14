package com.comercialvalerio.application.rest;
import java.util.List;

import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.dto.CategoriaCreateDto;
import com.comercialvalerio.application.dto.CategoriaDto;

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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CategoriaResourceApi {
    @GET
    public List<CategoriaDto> listar();
    @GET @Path("{id}")
    public CategoriaDto obtener(@PathParam("id") @NotNull Integer id);
    @POST
    public Response crear(@Valid CategoriaCreateDto dto);
    @PUT @Path("{id}")
    public CategoriaDto actualizar(@PathParam("id") @NotNull Integer id, @Valid CategoriaCreateDto dto);
    @DELETE @Path("{id}")
    public Response eliminar(@PathParam("id") @NotNull Integer id);
    @PUT @Path("{id}/estado")
    public Response cambiarEstado(@PathParam("id") @NotNull Integer id,
                                  @Valid CambiarEstadoDto dto,
                                  @jakarta.ws.rs.QueryParam("actualizarProductos")
                                  @jakarta.ws.rs.DefaultValue("true") boolean actualizarProductos);
    @GET @Path("{id}/eliminable")
    public List<String> obtenerDependencias(@PathParam("id") @NotNull Integer id);
}

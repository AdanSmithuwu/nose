package com.comercialvalerio.application.rest;
import com.comercialvalerio.application.dto.EstadoCreateDto;
import com.comercialvalerio.application.dto.EstadoDto;

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

@Path("/estados")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface EstadoResourceApi {
    @Context
    @GET
    public Response listar( @QueryParam("modulo") String modulo, @QueryParam("nombre") String nombre );

    /** Sobrecarga de conveniencia para proxies de UI. */
    default Response listar() {
        return listar(null, null);
    }
    @GET
    @Path("{id}")
    public EstadoDto obtener(@PathParam("id") @NotNull Integer id);
    @POST
    public Response crear(@Valid EstadoCreateDto dto);
    @PUT
    @Path("{id}")
    public EstadoDto actualizar(@PathParam("id") @NotNull Integer id, @Valid EstadoCreateDto dto);
    @DELETE
    @Path("{id}")
    public Response eliminar(@PathParam("id") @NotNull Integer id);
}

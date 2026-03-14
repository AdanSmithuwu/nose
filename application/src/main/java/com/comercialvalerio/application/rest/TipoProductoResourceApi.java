package com.comercialvalerio.application.rest;
import com.comercialvalerio.application.dto.TipoProductoCreateDto;
import com.comercialvalerio.application.dto.TipoProductoDto;

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

@Path("/tipos-producto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface TipoProductoResourceApi {
    @Context
    @GET
    Response listar(@QueryParam("nombre") String nombre);

    /** Sobrecarga de conveniencia para proxies de UI. */
    default Response listar() {
        return listar(null);
    }

    /** Envoltura de conveniencia usada por {@link com.comercialvalerio.application.service.TipoProductoService#buscarPorNombre}. */
    default Response buscarPorNombre(String nombre) {
        return listar(nombre);
    }

    @GET
    @Path("{id}")
    TipoProductoDto obtener(@PathParam("id") @NotNull Integer id);

    @POST
    Response crear(@Valid TipoProductoCreateDto dto);

    @PUT
    @Path("{id}")
    TipoProductoDto actualizar(@PathParam("id") @NotNull Integer id, @Valid TipoProductoCreateDto dto);

    @DELETE
    @Path("{id}")
    Response eliminar(@PathParam("id") @NotNull Integer id);
}

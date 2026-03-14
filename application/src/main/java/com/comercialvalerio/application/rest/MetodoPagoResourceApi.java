package com.comercialvalerio.application.rest;
import com.comercialvalerio.application.dto.MetodoPagoCreateDto;

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

@Path("/metodos-pago")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface MetodoPagoResourceApi {
    @Context
    @GET
    public Response listar( @QueryParam("nombre") String nombre );

    /** Sobrecarga de conveniencia para proxies de UI. */
    default Response listar() {
        return listar(null);
    }

    /** Envoltura de conveniencia usada por {@link com.comercialvalerio.application.service.MetodoPagoService#buscarPorNombre}. */
    default Response buscarPorNombre(String nombre) {
        return listar(nombre);
    }
    @GET @Path("{id}")
    public Response obtener(@PathParam("id") @NotNull Integer id);
    @POST
    public Response crear( @Valid MetodoPagoCreateDto dto );
    @PUT @Path("{id}")
    public Response actualizar(@PathParam("id") @NotNull Integer id, @Valid MetodoPagoCreateDto dto);
    @DELETE @Path("{id}")
    public Response eliminar(@PathParam("id") @NotNull Integer id);
}

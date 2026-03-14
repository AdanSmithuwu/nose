package com.comercialvalerio.application.rest;
import com.comercialvalerio.application.dto.ParametroSistemaCreateDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/parametros")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ParametroSistemaResourceApi {
    @GET
    public Response listar();
    @GET @Path("{clave}")
    public Response obtener(@PathParam("clave") @NotBlank String clave);
    @PUT @Path("{clave}")
    public Response guardar( @PathParam("clave") @NotBlank String clave, @Valid ParametroSistemaCreateDto dto);
}

package com.comercialvalerio.application.rest;
import java.time.LocalDateTime;

import com.comercialvalerio.application.dto.BitacoraLoginCreateDto;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/bitacoras")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface BitacoraLoginResourceApi {
    @GET
    @Path("/empleado/{idEmp}")
    @RolesAllowed("Administrador")
    public Response listarPorEmpleado(@PathParam("idEmp") @NotNull Integer idEmp);

    @GET
    @RolesAllowed("Administrador")
    public Response listarPorRango(@QueryParam("desde") LocalDateTime desde,
                                   @QueryParam("hasta") LocalDateTime hasta,
                                   @QueryParam("resultado") Boolean resultado);
    @POST
    public Response registrar(@Valid BitacoraLoginCreateDto dto);

    @POST @Path("/depurar")
    @RolesAllowed("Administrador")
    public Response depurarAntiguos();
}

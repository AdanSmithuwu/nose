package com.comercialvalerio.application.rest;
import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.dto.EmpleadoCreateDto;
import com.comercialvalerio.application.dto.EmpleadoCredencialesDto;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/empleados")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface EmpleadoResourceApi {
    @GET
    public Response listar();
    @GET
    @Path("{id}")
    public Response obtener(@PathParam("id") @NotNull Integer id);
    @POST
    public Response crear(@Valid EmpleadoCreateDto dto);
    @PUT
    @Path("{id}")
    public Response actualizar( @PathParam("id") @NotNull Integer id, @Valid EmpleadoCreateDto dto);
    @DELETE
    @Path("{id}")
    public Response eliminar(@PathParam("id") @NotNull Integer id);
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response autenticar(@FormParam("usuario") @NotBlank String usuario,
                               @FormParam("password") @NotBlank String pwd);
    @PUT
    @Path("{id}/estado")
    @RolesAllowed("Administrador")
    public Response cambiarEstado(@PathParam("id") @NotNull Integer id, @Valid CambiarEstadoDto dto);
    @PUT @Path("{id}/reset-clave")
    @RolesAllowed("Administrador")
    public Response resetClave(@PathParam("id") @NotNull Integer id, @NotBlank String nuevaClave);
    @PUT @Path("{id}/credenciales")
    @RolesAllowed("Administrador")
    public Response updateCredenciales(@PathParam("id") @NotNull Integer id, @Valid EmpleadoCredencialesDto dto);
    @GET @Path("{id}/eliminable")
    public Response obtenerDependencias(@PathParam("id") @NotNull Integer id);
    @POST
    @Path("/logout")
    public Response logout();
}

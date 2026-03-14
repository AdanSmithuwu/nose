package com.comercialvalerio.application.rest;
import java.net.URI;
import java.util.List;

import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.dto.EmpleadoCreateDto;
import com.comercialvalerio.application.dto.EmpleadoCredencialesDto;
import com.comercialvalerio.application.dto.EmpleadoDto;
import com.comercialvalerio.application.service.EmpleadoService;
import com.comercialvalerio.domain.security.RequestContext;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/empleados")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmpleadoResource extends BaseResource implements EmpleadoResourceApi {

    @Inject
    private EmpleadoService svc;

    @Context
    UriInfo uriInfo;

    @GET
    @Override
    public Response listar() {
        List<EmpleadoDto> todos = svc.listar();
        return ok(todos);
    }

    @GET
    @Path("{id}")
    @Override
    public Response obtener(@PathParam("id") @NotNull Integer id) {
        EmpleadoDto dto = svc.obtener(id);
        return ok(dto);
    }

    /*
     * Crea un nuevo empleado. El JSON puede incluir opcionalmente
     * el campo "plainPassword"; de lo contrario se generará uno
     * aleatoriamente y se devolverá en la respuesta.
     */
    @POST
    @Override
    public Response crear(@Valid EmpleadoCreateDto dto) {
        EmpleadoDto creado = svc.crear(dto);
        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(creado.idPersona().toString())
                              .build();
        return created(location, creado);
    }

    @PUT
    @Path("{id}")
    @RolesAllowed("Administrador")
    @Override
    public Response actualizar(
            @PathParam("id") @NotNull Integer id,
            @Valid EmpleadoCreateDto dto) {

        EmpleadoDto actualizado = svc.actualizar(id, dto);
        return ok(actualizado);
    }

    @DELETE
    @Path("{id}")
    @Override
    public Response eliminar(@PathParam("id") @NotNull Integer id) {
        svc.eliminar(id);
        return noContent();
    }

    /*
     * Autenticación de usuario.
     * Recibe form-urlencoded: usuario y password.
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Override
    public Response autenticar(
            @FormParam("usuario") @NotBlank String usuario,
            @FormParam("password") @NotBlank String pwd) {

        EmpleadoDto auth = svc.autenticar(usuario, pwd);
        return ok(auth);
    }

    @PUT
    @Path("{id}/estado")
    @RolesAllowed("Administrador")
    @Override
    public Response cambiarEstado(@PathParam("id") @NotNull Integer id,
                                  @Valid CambiarEstadoDto dto) {
      svc.cambiarEstado(id, dto);
      return noContent();
    }

    @PUT @Path("{id}/reset-clave")
    @RolesAllowed("Administrador")
    @Override
    public Response resetClave(@PathParam("id") @NotNull Integer id,
                               @NotBlank String nuevaClave) {
      svc.resetClave(id, nuevaClave);
      return noContent();
    }

    @PUT @Path("{id}/credenciales")
    @RolesAllowed("Administrador")
    @Override
    public Response updateCredenciales(@PathParam("id") @NotNull Integer id,
                                       @Valid EmpleadoCredencialesDto dto) {
      EmpleadoDto actualizado = svc.updateCredenciales(id, dto);
      return ok(actualizado);
    }

    @GET @Path("{id}/eliminable")
    @Override
    public Response obtenerDependencias(@PathParam("id") @NotNull Integer id) {
        List<String> deps = svc.obtenerDependencias(id);
        return ok(deps);
    }

    @POST
    @Path("/logout")
    @Override
    public Response logout() {
        RequestContext.clear();
        return noContent();
    }
}

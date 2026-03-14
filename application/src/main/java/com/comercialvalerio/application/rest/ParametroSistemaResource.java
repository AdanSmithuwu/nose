package com.comercialvalerio.application.rest;
import java.util.List;

import com.comercialvalerio.application.dto.ParametroSistemaCreateDto;
import com.comercialvalerio.application.dto.ParametroSistemaDto;
import com.comercialvalerio.application.service.ParametroSistemaService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.comercialvalerio.application.rest.BaseResource;
import jakarta.annotation.security.RolesAllowed;

@Path("/parametros")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ParametroSistemaResource extends BaseResource implements ParametroSistemaResourceApi {

    @Inject
    private ParametroSistemaService svc;

    @GET
    @RolesAllowed("Administrador")
    @Override
    public Response listar() {
        List<ParametroSistemaDto> todos = svc.listar();
        return ok(todos);
    }

    @GET @Path("{clave}")
    @RolesAllowed("Administrador")
    @Override
    public Response obtener(@PathParam("clave") @NotBlank String clave) {
        ParametroSistemaDto dto = svc.obtener(clave);
        return ok(dto);
    }

    @PUT @Path("{clave}")
    @RolesAllowed("Administrador")
    @Override
    public Response guardar(
            @PathParam("clave") @NotBlank String clave,
            @Valid ParametroSistemaCreateDto dto) {

        if (!clave.equalsIgnoreCase(dto.clave())) {
            throw new BadRequestException("La clave del path y del body deben coincidir");
        }

        ParametroSistemaDto actualizado = svc.guardar(clave, dto);
        return ok(actualizado);
    }
}

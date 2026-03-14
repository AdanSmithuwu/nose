package com.comercialvalerio.application.rest;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.application.dto.BitacoraLoginCreateDto;
import com.comercialvalerio.application.dto.BitacoraLoginDto;
import com.comercialvalerio.application.service.BitacoraLoginService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/bitacoras")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BitacoraLoginResource extends BaseResource implements BitacoraLoginResourceApi {

    @Inject
    private BitacoraLoginService svc;

    @Context
    UriInfo uriInfo;

    /* Lista todos los eventos de un empleado. */
    @GET
    @Path("/empleado/{idEmp}")
    @RolesAllowed("Administrador")
    @Override
    public Response listarPorEmpleado(@PathParam("idEmp") @NotNull Integer idEmp) {
        List<BitacoraLoginDto> lista = svc.listarPorEmpleado(idEmp);
        return ok(lista);
    }

    /* Lista todos los eventos en un rango de fechas ISO-8601. */
    @GET
    @RolesAllowed("Administrador")
    @Override
    public Response listarPorRango(
      @QueryParam("desde") LocalDateTime desde,
      @QueryParam("hasta") LocalDateTime hasta,
      @QueryParam("resultado") Boolean resultado
    ) {
        if (desde == null || hasta == null) {
            throw new BadRequestException("Debe indicar desde y hasta");
        }
        List<BitacoraLoginDto> lista = svc.listarPorRango(desde, hasta, resultado);
        return ok(lista);
    }

    /*
     * Registra un nuevo evento en la bitácora de login.
     * Retorna 201 Created con Location: /bitacoras/{idBitacora}.
     */
    @POST
    @Override
    public Response registrar(@Valid BitacoraLoginCreateDto dto) {
        BitacoraLoginDto creado = svc.registrar(dto);
        URI uri = uriInfo.getAbsolutePathBuilder()
                         .path(creado.idBitacora().toString())
                         .build();
        return created(uri, creado);
    }

    @POST
    @Path("/depurar")
    @RolesAllowed("Administrador")
    @Override
    public Response depurarAntiguos() {
        svc.depurarAntiguos();
        return noContent();
    }
}

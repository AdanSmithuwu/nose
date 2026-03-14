package com.comercialvalerio.application.rest;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.application.dto.MovimientoInventarioCreateDto;
import com.comercialvalerio.application.dto.MovimientoInventarioDto;
import com.comercialvalerio.application.service.MovimientoInventarioService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
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
import jakarta.annotation.security.RolesAllowed;

@RequestScoped
@Path("/movimientos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MovimientoInventarioResource implements MovimientoInventarioResourceApi {

    @Inject
    MovimientoInventarioService svc;

    /* ---------- GET / producto/{id} ---------- */
    @GET
    @Path("/producto/{idProd}")
    @RolesAllowed("Administrador")
    @Override
    public List<MovimientoInventarioDto> listarPorProducto(@PathParam("idProd") @NotNull Integer id) {
        return svc.listarPorProducto(id);
    }

    /* ---------- GET ?d=2025-05-01T00:00&h=2025-05-31T23:59 ---------- */
    @GET
    @RolesAllowed("Administrador")
    @Override
    public List<MovimientoInventarioDto> listarPorRango(
            @QueryParam("d") LocalDateTime d,
            @QueryParam("h") LocalDateTime h) {
        return svc.listarPorRango(d, h);
    }

    /* ---------- POST ---------- */
    @POST
    @Override
    public MovimientoInventarioDto registrar(@Valid MovimientoInventarioCreateDto dto) {
        return svc.registrar(dto);
    }
}

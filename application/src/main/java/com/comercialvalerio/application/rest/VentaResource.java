package com.comercialvalerio.application.rest;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.application.dto.MotivoDto;
import com.comercialvalerio.application.dto.VentaCreateDto;
import com.comercialvalerio.application.dto.VentaDto;
import com.comercialvalerio.application.service.VentaService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.comercialvalerio.application.rest.util.Responses;

@Path("/ventas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VentaResource implements VentaResourceApi {

    @Inject
    VentaService svc;

    /* ----- consultas ----- */
    @GET
    @Override
    public List<VentaDto> listar() { return svc.listar(); }

    @GET @Path("/rango")
    @Override
    public List<VentaDto> listarPorRango(@QueryParam("d") LocalDateTime d,
                                   @QueryParam("h") LocalDateTime h) {
        return svc.listarPorRango(d, h);
    }

    @GET @Path("/cliente/{id}")
    @Override
    public List<VentaDto> listarPorCliente(@PathParam("id") @NotNull Integer id) {
        return svc.listarPorCliente(id);
    }

    @GET @Path("{id}")
    @Override
    public VentaDto obtener(@PathParam("id") @NotNull Integer id) {
        return svc.obtener(id);
    }

    /* ----- comando ----- */
    @POST
    @Override
    public VentaDto crear(@Valid VentaCreateDto dto) { return svc.crear(dto); }

    @PUT @Path("{id}/cancelar")
    @Override
    public Response cancelar(
        @PathParam("id") @NotNull Integer id,
        @Valid MotivoDto body
    ) {
        svc.cancelar(id, body);
        return Responses.noContent();
    }
}

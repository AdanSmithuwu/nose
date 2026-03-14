package com.comercialvalerio.application.rest;

import java.util.List;

import com.comercialvalerio.application.dto.AlertaStockDto;
import com.comercialvalerio.application.service.AlertaStockService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.comercialvalerio.application.rest.util.Responses;
import jakarta.validation.constraints.NotNull;

@RequestScoped
@Path("/alertas-stock")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlertaStockResource implements AlertaStockResourceApi {

    @Inject
    AlertaStockService svc;

    @GET
    @Override
    public List<AlertaStockDto> listarPendientes() {
        return svc.listarPendientes();
    }

    @PUT
    @Path("{id}/procesada")
    @Override
    public Response marcarProcesada(@PathParam("id") @NotNull Integer id) {
        svc.marcarProcesada(id);
        return Responses.noContent();
    }

    @PUT
    @Path("{id}/procesar")
    @Override
    public Response procesarProducto(@PathParam("id") @NotNull Integer id) {
        svc.procesarProducto(id);
        return Responses.noContent();
}
}

package com.comercialvalerio.application.rest;
import java.util.List;

import com.comercialvalerio.application.dto.DetalleDto;
import com.comercialvalerio.application.service.DetalleService;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/*
 * Solo lectura: la mutación se realiza estrictamente al crear Venta/Pedido.
 */
@Path("/transacciones/{idTx}/detalles")
@Produces(MediaType.APPLICATION_JSON)
public class DetalleResource implements DetalleResourceApi {

    @Inject
    DetalleService svc;

    @GET
    @Override
    public List<DetalleDto> listar(@PathParam("idTx") @NotNull Integer idTx) {
        return svc.listar(idTx);
    }
}

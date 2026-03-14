package com.comercialvalerio.application.rest;

import java.util.List;

import com.comercialvalerio.application.dto.HistorialTransaccionDto;
import com.comercialvalerio.application.service.HistorialTransaccionService;
import com.comercialvalerio.application.service.ClienteService;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/historial")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HistorialTransaccionResource implements HistorialTransaccionResourceApi {

    @Inject
    HistorialTransaccionService svc;

    @Inject
    ClienteService clienteService;

    @GET
    @Path("/cliente/{id}")
    @Override
    public List<HistorialTransaccionDto> listarPorCliente(@PathParam("id") @NotNull Integer id) {
        // Validar que el cliente exista para no devolver una lista vacía
        clienteService.obtener(id);
        return svc.listarPorCliente(id);
    }
}

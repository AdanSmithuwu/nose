package com.comercialvalerio.application.rest;
import java.util.List;
import com.comercialvalerio.application.dto.AlertaStockDto;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.validation.constraints.NotNull;

@Path("/alertas-stock")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AlertaStockResourceApi {
    @GET
    public List<AlertaStockDto> listarPendientes();
    @PUT
    @Path("{id}/procesada")
    public Response marcarProcesada(@PathParam("id") @NotNull Integer id);

    @PUT
    @Path("{id}/procesar")
    public Response procesarProducto(@PathParam("id") @NotNull Integer id);
}

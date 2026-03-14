package com.comercialvalerio.application.rest;
import java.util.List;

import com.comercialvalerio.application.dto.PagoCreateDto;
import com.comercialvalerio.application.dto.PagoDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/transacciones/{idTx}/pagos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface PagoResourceApi {
    @GET
    public List<PagoDto> listar(@PathParam("idTx") @NotNull Integer idTx);
    @POST
    public Response registrar(@PathParam("idTx") @NotNull Integer idTx, @Valid PagoCreateDto dto);
    @DELETE
    @Path("{idPago}")
    public Response eliminar( @PathParam("idTx")   @NotNull Integer idTx, @PathParam("idPago") @NotNull Integer idPago );
}

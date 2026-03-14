package com.comercialvalerio.application.rest;
import java.net.URI;
import java.util.List;

import com.comercialvalerio.application.dto.PagoCreateDto;
import com.comercialvalerio.application.dto.PagoDto;
import com.comercialvalerio.application.service.PagoService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@RequestScoped
@Path("/transacciones/{idTx}/pagos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PagoResource extends BaseResource implements PagoResourceApi {

    @Inject
    PagoService svc;

    @Context
    UriInfo uri;

    /* -------- listado -------- */
    @GET
    @Override
    public List<PagoDto> listar(@PathParam("idTx") @NotNull Integer idTx) {
        return svc.listar(idTx);
    }

    /* -------- crear -------- */
    @POST
    @Override
    public Response registrar(@PathParam("idTx") @NotNull Integer idTx,
                          @Valid PagoCreateDto dto) {

        PagoDto dtoCreated = svc.registrar(idTx, dto);
        URI loc = uri.getAbsolutePathBuilder()
                     .path(dtoCreated.idMetodoPago().toString())
                     .build();
        return created(loc, dtoCreated);
    }

    /* -------- eliminar -------- */
    @DELETE
    @Path("{idPago}")
    @Override
    public Response eliminar(
        @PathParam("idTx")   @NotNull Integer idTx,
        @PathParam("idPago") @NotNull Integer idPago
    ) {
        svc.eliminar(idTx, idPago);
        return noContent();
    }
}

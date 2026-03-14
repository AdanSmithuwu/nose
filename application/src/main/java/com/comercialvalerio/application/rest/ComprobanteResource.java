package com.comercialvalerio.application.rest;
import java.net.URI;

import com.comercialvalerio.application.dto.ComprobanteDto;
import com.comercialvalerio.application.dto.ComprobantePdfDto;
import com.comercialvalerio.application.dto.TelefonoDto;
import com.comercialvalerio.application.service.ComprobanteService;
import com.comercialvalerio.application.exception.PdfGenerationException;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.validation.constraints.NotNull;
import com.comercialvalerio.application.rest.BaseResource;

@RequestScoped
@Path("/transacciones/{idTx}/comprobante")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ComprobanteResource extends BaseResource implements ComprobanteResourceApi {

    @Inject
    ComprobanteService svc;

    @Context
    UriInfo uri;

    /* ---------- POST / genera ---------- */
    @POST
    @Override
    public Response generar(@PathParam("idTx") Integer idTx) throws PdfGenerationException {

        ComprobanteDto creado = svc.generar(idTx);
        URI loc = uri.getAbsolutePath();     // único por transacción
        return created(loc, creado);
    }

    /* ---------- GET / ---------- */
    @GET
    @Override
    public ComprobanteDto obtenerPorTransaccion(@PathParam("idTx") Integer idTx) {
        return svc.obtenerPorTransaccion(idTx);
    }

    @GET
    @Path("/pdf")
    @Produces("application/pdf")
    @Override
    public Response descargarPdf(@PathParam("idTx") Integer idTx) {
        var dto = svc.obtenerPdf(idTx);
        byte[] pdf = dto.pdf();
        String disposition = "attachment; filename=" + dto.nombreArchivo();
        return Response.ok(pdf)
                .header("Content-Disposition", disposition)
                .build();
    }

    @GET
    @Path("/pdf/datos")
    @Override
    public ComprobantePdfDto obtenerPdf(@PathParam("idTx") Integer idTx) {
        return svc.obtenerPdf(idTx);
    }

    @POST
    @Path("/whatsapp")
    @Override
    public Response enviarWhatsApp(@PathParam("idTx") Integer idTx, TelefonoDto dto) {
        svc.enviarWhatsApp(idTx, dto);
        return Response.accepted().build();
    }
}

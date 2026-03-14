package com.comercialvalerio.application.rest;
import com.comercialvalerio.application.dto.ComprobanteDto;
import com.comercialvalerio.application.dto.ComprobantePdfDto;
import com.comercialvalerio.application.dto.TelefonoDto;
import com.comercialvalerio.application.exception.PdfGenerationException;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/transacciones/{idTx}/comprobante")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ComprobanteResourceApi {
    @POST
    public Response generar(@PathParam("idTx") Integer idTx) throws PdfGenerationException;
    @GET
    public ComprobanteDto obtenerPorTransaccion(@PathParam("idTx") Integer idTx);
    @GET
    @Path("/pdf")
    @Produces("application/pdf")
    public Response descargarPdf(@PathParam("idTx") Integer idTx);

    @GET
    @Path("/pdf/datos")
    public ComprobantePdfDto obtenerPdf(@PathParam("idTx") Integer idTx);

    @POST
    @Path("/whatsapp")
    public Response enviarWhatsApp(@PathParam("idTx") Integer idTx, TelefonoDto dto);
}

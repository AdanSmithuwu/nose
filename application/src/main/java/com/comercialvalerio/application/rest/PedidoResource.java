package com.comercialvalerio.application.rest;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.application.dto.MotivoDto;
import com.comercialvalerio.application.dto.PagoCreateDto;
import com.comercialvalerio.application.dto.PedidoCreateDto;
import com.comercialvalerio.application.dto.PedidoDto;
import com.comercialvalerio.application.dto.PedidoPendienteDto;
import com.comercialvalerio.application.dto.TelefonoDto;
import com.comercialvalerio.application.dto.OrdenCompraPdfDto;
import com.comercialvalerio.application.service.PedidoService;
import com.comercialvalerio.application.exception.PdfGenerationException;

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

@Path("/pedidos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PedidoResource implements PedidoResourceApi {

    @Inject
    PedidoService svc;

    /* ----- consultas ----- */
    @GET
    @Override
    public List<PedidoDto> listar() { return svc.listar(); }
    @GET @Path("/pendientes")
    @Override
    public List<PedidoPendienteDto> listarPendientes() {
        return svc.listarPendientes();
    }
    @GET @Path("/rango")
    @Override
    public List<PedidoDto> listarPorRango(@QueryParam("d") LocalDateTime d,
                                    @QueryParam("h") LocalDateTime h) {
        return svc.listarPorRango(d, h);
    }

    @GET @Path("{id}")
    @Override
    public PedidoDto obtener(@PathParam("id") @NotNull Integer id) {
        return svc.obtener(id);
    }

    /* ----- comando ----- */
    @POST
    @Override
    public PedidoDto crear(@Valid PedidoCreateDto dto) throws PdfGenerationException {
        return svc.crear(dto);
    }

    @PUT @Path("{id}")
    @Override
    public PedidoDto actualizar(@PathParam("id") @NotNull Integer id,
                                @Valid PedidoCreateDto dto) {
        return svc.actualizar(id, dto);
    }

    @PUT @Path("{id}/cancelar")
    @Override
    public Response cancelar(@PathParam("id") @NotNull Integer id,
                             @Valid MotivoDto m) {
        svc.cancelar(id, m);
        return Responses.noContent();
    }

    /* Nuevo: marcar entregado **/
    @PUT @Path("{id}/entregar")
    @Override
    public Response marcarEntregado(
        @PathParam("id") @NotNull Integer id,
        @Valid List<PagoCreateDto> pagos
    ) throws PdfGenerationException {
        svc.marcarEntregado(id, pagos);
        return Responses.noContent();
    }

    @GET @Path("{id}/faltantes")
    @Override
    public List<String> verificarStockEntrega(@PathParam("id") @NotNull Integer id) {
        return svc.verificarStockEntrega(id);
    }

    @GET
    @Path("{id}/orden")
    @Produces("application/pdf")
    @Override
    public Response descargarOrden(@PathParam("id") @NotNull Integer id) {
        var dto = svc.obtenerOrden(id);
        byte[] pdf = dto.pdf();
        String dispo = "inline; filename=" + dto.nombreArchivo();
        return Response.ok(pdf)
                .header("Content-Disposition", dispo)
                .build();
    }

    @GET
    @Path("{id}/orden/datos")
    @Override
    public OrdenCompraPdfDto obtenerOrden(@PathParam("id") @NotNull Integer id) {
        return svc.obtenerOrden(id);
    }

    @POST
    @Path("{id}/orden/whatsapp")
    @Override
    public Response enviarOrdenWhatsApp(@PathParam("id") @NotNull Integer id, TelefonoDto dto) {
        svc.enviarOrdenWhatsApp(id, dto);
        return Response.accepted().build();
    }
}

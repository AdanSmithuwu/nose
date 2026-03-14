package com.comercialvalerio.application.rest;
import java.time.LocalDateTime;
import java.util.List;
import com.comercialvalerio.application.dto.MotivoDto;
import com.comercialvalerio.application.dto.PagoCreateDto;
import com.comercialvalerio.application.dto.PedidoCreateDto;
import com.comercialvalerio.application.dto.PedidoDto;
import com.comercialvalerio.application.dto.PedidoPendienteDto;
import com.comercialvalerio.application.dto.OrdenCompraPdfDto;
import com.comercialvalerio.application.dto.TelefonoDto;
import com.comercialvalerio.application.exception.PdfGenerationException;
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

@Path("/pedidos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PedidoResourceApi {
    @GET
    public List<PedidoDto> listar();
    @GET @Path("/pendientes")
    public List<PedidoPendienteDto> listarPendientes();
    @GET @Path("/rango")
    public List<PedidoDto> listarPorRango(@QueryParam("d") LocalDateTime d, @QueryParam("h") LocalDateTime h);
    @GET @Path("{id}")
    public PedidoDto obtener(@PathParam("id") @NotNull Integer id);
    @POST
    public PedidoDto crear(@Valid PedidoCreateDto dto) throws PdfGenerationException;
    @PUT @Path("{id}")
    public PedidoDto actualizar(@PathParam("id") @NotNull Integer id, @Valid PedidoCreateDto dto);
    @PUT @Path("{id}/cancelar")
    public Response cancelar(@PathParam("id") @NotNull Integer id, @Valid MotivoDto m);
    @PUT @Path("{id}/entregar")
    public Response marcarEntregado( @PathParam("id") @NotNull Integer id,
                              @Valid List<PagoCreateDto> pagos )
                              throws PdfGenerationException;
    @GET @Path("{id}/faltantes")
    public List<String> verificarStockEntrega(@PathParam("id") @NotNull Integer id);
    @GET
    @Path("{id}/orden")
    @Produces("application/pdf")
    public Response descargarOrden(@PathParam("id") @NotNull Integer id);

    @GET
    @Path("{id}/orden/datos")
    public OrdenCompraPdfDto obtenerOrden(@PathParam("id") @NotNull Integer id);

    @POST
    @Path("{id}/orden/whatsapp")
    public Response enviarOrdenWhatsApp(@PathParam("id") @NotNull Integer id, TelefonoDto dto);
}

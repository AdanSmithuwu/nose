package com.comercialvalerio.application.service;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.application.dto.PagoCreateDto;
import com.comercialvalerio.application.dto.PedidoCreateDto;
import com.comercialvalerio.application.dto.PedidoDto;
import com.comercialvalerio.application.dto.PedidoPendienteDto;
import com.comercialvalerio.application.dto.MotivoDto;
import com.comercialvalerio.application.dto.TelefonoDto;
import com.comercialvalerio.application.dto.OrdenCompraPdfDto;
import com.comercialvalerio.application.exception.PdfGenerationException;

public interface PedidoService {
    List<PedidoDto> listar();
    List<PedidoPendienteDto> listarPendientes();
    List<PedidoDto> listarPorRango(LocalDateTime d, LocalDateTime h);
    PedidoDto       obtener(Integer id);
    PedidoDto       crear(PedidoCreateDto dto) throws PdfGenerationException;
    PedidoDto       actualizar(Integer id, PedidoCreateDto dto);
    void            cancelar(Integer idPedido, MotivoDto motivo);
    void            marcarEntregado(Integer id, List<PagoCreateDto> pagos)
                    throws PdfGenerationException;
    /**
     * Verifica si hay stock suficiente para entregar un pedido especial.
     *
     * @param idPedido identificador del pedido
     * @return lista de nombres de productos con stock insuficiente
     */
    List<String>    verificarStockEntrega(Integer idPedido);
    byte[]          descargarOrden(Integer idPedido);
    /** Obtiene el PDF de la orden con sus metadatos. */
    OrdenCompraPdfDto obtenerOrden(Integer idPedido);
    void            enviarOrdenWhatsApp(Integer idPedido, TelefonoDto telefono);
}

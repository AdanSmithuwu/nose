package com.comercialvalerio.infrastructure.persistence.dto;

import java.time.LocalDateTime;

/**
 * Proyección sencilla para los resultados de {@code sp_ListarPedidosPendientes}.
 */
public record PedidoPendienteDto(Integer idTransaccion,
                                 LocalDateTime fecha,
                                 Integer idEmpleado,
                                 Integer idCliente,
                                 String direccionEntrega,
                                 String tipoPedido,
                                 Boolean usaValeGas) {}

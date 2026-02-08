package com.comercialvalerio.domain.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Datos de una transacci\u00f3n perteneciente al historial de un cliente. */
public record HistorialTransaccionView(Integer idTransaccion,
                                       Integer idCliente,
                                       String cliente,
                                       LocalDateTime fecha,
                                       BigDecimal totalNeto,
                                       BigDecimal descuento,
                                       BigDecimal cargo,
                                       String estado,
                                       String tipo) {}

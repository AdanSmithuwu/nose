package com.comercialvalerio.domain.view.report;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos de ventas y pedidos por día. */
public record TransaccionesDia(LocalDate dia,
                               Long numVentas,
                               Long numPedidos,
                               BigDecimal montoBruto,
                               BigDecimal montoNeto,
                               BigDecimal ingresosDia) {
    public long numTransacciones() {
        long v = numVentas == null ? 0L : numVentas;
        long p = numPedidos == null ? 0L : numPedidos;
        return v + p;
    }
}

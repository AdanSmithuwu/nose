package com.comercialvalerio.domain.view.report;

import java.math.BigDecimal;

/** Producto más vendido de un mes o rango. */
public record RotacionProducto(int anio,
                               int mes,
                               int posicion,
                               int idProducto,
                               String producto,
                               BigDecimal totalUnidadesVendidas,
                               String categoria,
                               BigDecimal importeTotal) {}

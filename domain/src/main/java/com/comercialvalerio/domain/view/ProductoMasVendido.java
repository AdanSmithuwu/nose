package com.comercialvalerio.domain.view;

import java.math.BigDecimal;

/** Datos de productos más vendidos. */
public record ProductoMasVendido(Integer idProducto,
                                 String nombre,
                                 BigDecimal unidadesVendidas,
                                 BigDecimal ingresos) {}

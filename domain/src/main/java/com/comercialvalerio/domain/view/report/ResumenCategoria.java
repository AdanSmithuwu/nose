package com.comercialvalerio.domain.view.report;

import java.math.BigDecimal;

/** Resumen mensual por categoría de productos. */
public record ResumenCategoria(int anio,
                               int mes,
                               String categoria,
                               long numTransacciones,
                               BigDecimal ingresosCategoria) {}

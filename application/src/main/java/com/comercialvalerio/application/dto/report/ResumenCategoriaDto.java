package com.comercialvalerio.application.dto.report;

import java.math.BigDecimal;

public record ResumenCategoriaDto(int anio,
                                  int mes,
                                  String categoria,
                                  long numTransacciones,
                                  BigDecimal ingresosCategoria) {}

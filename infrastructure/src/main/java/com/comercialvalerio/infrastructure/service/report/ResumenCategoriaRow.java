package com.comercialvalerio.infrastructure.service.report;

import java.math.BigDecimal;

public record ResumenCategoriaRow(String categoria, Long numTransacciones, BigDecimal ingresos) {}

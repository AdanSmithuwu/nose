package com.comercialvalerio.infrastructure.service.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionDiaRow(LocalDate dia, Long numTransacciones, BigDecimal ingresos) {}

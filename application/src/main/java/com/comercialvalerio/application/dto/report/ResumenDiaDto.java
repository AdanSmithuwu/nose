package com.comercialvalerio.application.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumenDiaDto(
        LocalDate fecha,
        long numTransacciones,
        BigDecimal monto) {}

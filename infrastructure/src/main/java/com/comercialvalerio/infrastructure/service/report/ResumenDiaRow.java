package com.comercialvalerio.infrastructure.service.report;

import java.math.BigDecimal;

public record ResumenDiaRow(Long numTransacciones, Long numPedidos,
                             BigDecimal totalBruto, BigDecimal totalNeto) {}

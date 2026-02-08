package com.comercialvalerio.infrastructure.service.report;

import java.math.BigDecimal;

public record RotacionRow(Integer posicion, String producto, String categoria,
                          BigDecimal unidades, BigDecimal importe) {}

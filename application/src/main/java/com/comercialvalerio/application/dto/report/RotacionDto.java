package com.comercialvalerio.application.dto.report;

import java.math.BigDecimal;

public record RotacionDto(int posicion, String producto, String categoria, BigDecimal unidades, BigDecimal importe) {}

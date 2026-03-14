package com.comercialvalerio.application.dto;

import java.math.BigDecimal;

public record ProductoMasVendidoDto(
    Integer idProducto,
    String  nombre,
    BigDecimal unidadesVendidas,
    BigDecimal ingresos
) {}

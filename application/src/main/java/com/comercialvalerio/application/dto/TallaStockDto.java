package com.comercialvalerio.application.dto;
import java.math.BigDecimal;

public record TallaStockDto(
    Integer    idTallaStock,
    Integer    productoId,
    String     productoNombre,
    String     talla,
    BigDecimal stock,
    String     estado
) {}

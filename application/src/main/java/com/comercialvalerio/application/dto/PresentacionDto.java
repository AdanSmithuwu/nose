package com.comercialvalerio.application.dto;
import java.math.BigDecimal;

public record PresentacionDto(
    Integer idPresentacion,
    Integer productoId,
    String  productoNombre,
    BigDecimal cantidad,
    BigDecimal precio,
    String  estado
) {}

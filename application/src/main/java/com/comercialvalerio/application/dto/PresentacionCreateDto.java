package com.comercialvalerio.application.dto;
import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Digits;
import com.comercialvalerio.common.DbConstraints;

public record PresentacionCreateDto(
    @NotNull(message = "idProducto obligatorio")
    Integer idProducto,
    @NotNull(message = "cantidad obligatoria")
    @Positive(message = "cantidad debe ser mayor que 0")
    @Digits(integer = DbConstraints.CANTIDAD_INTEGER,
            fraction = DbConstraints.CANTIDAD_SCALE)
    BigDecimal cantidad,

    @NotNull(message = "precio obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "precio no puede ser negativo")
    @Digits(integer = DbConstraints.PRECIO_INTEGER,
            fraction = DbConstraints.PRECIO_SCALE)
    BigDecimal precio
) {}

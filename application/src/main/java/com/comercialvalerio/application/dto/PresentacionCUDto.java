package com.comercialvalerio.application.dto;
import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;
import com.comercialvalerio.common.DbConstraints;

public record PresentacionCUDto(
    Integer idPresentacion, // null = nueva
    @NotNull
    @DecimalMin(value = "0.000", inclusive = false,
                message = "cantidad debe ser mayor que 0")
    @Digits(integer = DbConstraints.CANTIDAD_INTEGER,
            fraction = DbConstraints.CANTIDAD_SCALE)
    BigDecimal cantidad,
    @NotNull @DecimalMin("0.0")
    @Digits(integer = DbConstraints.PRECIO_INTEGER,
            fraction = DbConstraints.PRECIO_SCALE)
    BigDecimal precio
) {}

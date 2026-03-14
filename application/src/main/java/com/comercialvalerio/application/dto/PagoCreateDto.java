package com.comercialvalerio.application.dto;
import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Digits;
import com.comercialvalerio.common.DbConstraints;

public record PagoCreateDto(
    @NotNull(message = "idMetodoPago obligatorio")
    Integer idMetodoPago,
    @NotNull(message = "monto obligatorio")
    @Positive(message = "monto debe ser mayor que 0")
    @Digits(integer = DbConstraints.PRECIO_INTEGER,
            fraction = DbConstraints.PRECIO_SCALE)
    BigDecimal monto

) {}

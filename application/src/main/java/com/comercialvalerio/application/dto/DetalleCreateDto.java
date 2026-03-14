package com.comercialvalerio.application.dto;
import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import com.comercialvalerio.common.DbConstraints;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DetalleCreateDto(
    @NotNull(message = "idProducto obligatorio")
    Integer idProducto,
    // nulo para productos sin talla
    Integer idTallaStock,
    @NotNull(message = "cantidad obligatoria")
    @DecimalMin(value = "0.000", inclusive = false)
    @Positive(message = "cantidad debe ser mayor que 0")
    @Digits(integer = DbConstraints.STOCK_INTEGER,
            fraction = DbConstraints.STOCK_SCALE)
    BigDecimal cantidad,
    @NotNull(message = "precioUnitario obligatorio")
    @DecimalMin(value = "0.00", inclusive = true, message = "precioUnitario no puede ser negativo")
    @Digits(integer = DbConstraints.PRECIO_INTEGER,
            fraction = DbConstraints.PRECIO_SCALE)
    BigDecimal precioUnitario

) {}

package com.comercialvalerio.application.dto;
import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import com.comercialvalerio.common.DbConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TallaStockCreateDto(
    @NotNull(message = "idProducto obligatorio")
    Integer idProducto,
    @NotBlank(message = "talla obligatoria")
    @Size(max = DbConstraints.LEN_TALLA,
          message = "talla máximo " + DbConstraints.LEN_TALLA + " caracteres")
    String talla,
    @NotNull(message = "stock obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "stock no puede ser negativo")
    @Digits(integer = DbConstraints.STOCK_INTEGER,
            fraction = DbConstraints.STOCK_SCALE)
    BigDecimal stock
) {}

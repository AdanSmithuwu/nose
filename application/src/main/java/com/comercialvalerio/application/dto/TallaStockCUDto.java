package com.comercialvalerio.application.dto;
import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.comercialvalerio.common.DbConstraints;

public record TallaStockCUDto(
    Integer idTallaStock, // null = nueva
    @NotBlank @Size(max = DbConstraints.LEN_TALLA) String talla,
    @NotNull
    @DecimalMin("0.0")
    @Digits(integer = DbConstraints.STOCK_INTEGER,
            fraction = DbConstraints.STOCK_SCALE)
    BigDecimal stock
) {}

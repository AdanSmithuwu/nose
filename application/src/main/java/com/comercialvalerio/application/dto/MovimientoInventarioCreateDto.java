package com.comercialvalerio.application.dto;
import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;
import com.comercialvalerio.common.DbConstraints;

public record MovimientoInventarioCreateDto(
    @NotNull(message = "idProducto obligatorio")
    Integer idProducto,
    /* opcional */
    Integer idTallaStock,
    @NotNull(message = "idTipoMov obligatorio")
    Integer idTipoMov,
    @NotNull(message = "cantidad obligatoria")
    @Positive(message = "cantidad debe ser mayor que 0")
    @Digits(integer = DbConstraints.STOCK_INTEGER,
            fraction = DbConstraints.STOCK_SCALE)
    BigDecimal cantidad,
    @Size(max = DbConstraints.LEN_MOTIVO,
          message = "motivo máximo " + DbConstraints.LEN_MOTIVO + " caracteres")
    String motivo,
    @NotNull(message = "idEmpleado obligatorio")
    Integer idEmpleado
) {}

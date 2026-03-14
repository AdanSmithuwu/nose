package com.comercialvalerio.application.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.comercialvalerio.common.DbConstraints;

/** Datos necesarios para registrar una Venta. */

public record VentaCreateDto(
        @NotNull @DecimalMin("0.00")
        @Digits(integer = DbConstraints.PRECIO_INTEGER,
                fraction = DbConstraints.PRECIO_SCALE)
        BigDecimal totalBruto,
        @NotNull @DecimalMin("0.00")
        @Digits(integer = DbConstraints.PRECIO_INTEGER,
                fraction = DbConstraints.PRECIO_SCALE)
        BigDecimal descuento,
        @NotNull @DecimalMin("0.00")
        @Digits(integer = DbConstraints.PRECIO_INTEGER,
                fraction = DbConstraints.PRECIO_SCALE)
        BigDecimal cargo,
        @NotNull @DecimalMin("0.00")
        @Digits(integer = DbConstraints.PRECIO_INTEGER,
                fraction = DbConstraints.PRECIO_SCALE)
        BigDecimal totalNeto,
        @Size(max = DbConstraints.LEN_OBSERVACION)
                                String  observacion,
        @NotNull                       Integer  idEmpleado,
        @NotNull                       Integer  idCliente,
        @NotEmpty                      List<DetalleCreateDto> detalles,
        @NotEmpty                      List<PagoCreateDto>    pagos,
                                        ClienteCreateDto     nuevoCliente
) {}

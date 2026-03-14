package com.comercialvalerio.application.dto;
import java.math.BigDecimal;
import java.util.List;

import com.comercialvalerio.common.DbConstraints;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PedidoCreateDto(
        /* cabecera */
        @NotNull @DecimalMin("0.00")
        @Digits(integer = DbConstraints.PRECIO_INTEGER,
                fraction = DbConstraints.PRECIO_SCALE,
                message = "totalBruto debe tener hasta " + DbConstraints.PRECIO_INTEGER
                        + " enteros y " + DbConstraints.PRECIO_SCALE + " decimales")
        BigDecimal totalBruto,
        @NotNull @DecimalMin("0.00")
        @Digits(integer = DbConstraints.PRECIO_INTEGER,
                fraction = DbConstraints.PRECIO_SCALE,
                message = "descuento debe tener hasta " + DbConstraints.PRECIO_INTEGER
                        + " enteros y " + DbConstraints.PRECIO_SCALE + " decimales")
        BigDecimal descuento,
        @NotNull @DecimalMin("0.00")
        @Digits(integer = DbConstraints.PRECIO_INTEGER,
                fraction = DbConstraints.PRECIO_SCALE,
                message = "cargo debe tener hasta " + DbConstraints.PRECIO_INTEGER
                        + " enteros y " + DbConstraints.PRECIO_SCALE + " decimales")
        BigDecimal cargo,
        @NotNull @DecimalMin("0.00")
        @Digits(integer = DbConstraints.PRECIO_INTEGER,
                fraction = DbConstraints.PRECIO_SCALE,
                message = "totalNeto debe tener hasta " + DbConstraints.PRECIO_INTEGER
                        + " enteros y " + DbConstraints.PRECIO_SCALE + " decimales")
        BigDecimal totalNeto,
        @Size(max = DbConstraints.LEN_OBSERVACION)
                                String  observacion,
        @NotNull                       Integer  idEmpleado,
        @NotNull                       Integer  idCliente,
        /* propios pedido */
        @NotBlank @Size(max = DbConstraints.LEN_DIRECCION)
        String  direccionEntrega,
        @NotNull                     TipoPedido tipoPedido,
                                         boolean usaValeGas,
        /* cuerpo */
        @NotEmpty                      List<DetalleCreateDto> detalles
) {}

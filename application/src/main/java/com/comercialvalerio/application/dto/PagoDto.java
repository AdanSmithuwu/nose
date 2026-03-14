package com.comercialvalerio.application.dto;
import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PagoDto(
    Integer      idPago,
    Integer      idTransaccion,
    @NotNull Integer   idMetodoPago,
    String       metodoNombre,
    @NotNull @DecimalMin("0.01") BigDecimal monto
) {}

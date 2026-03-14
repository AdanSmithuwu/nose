package com.comercialvalerio.application.dto;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import com.comercialvalerio.common.json.OffsetDateTimeAdapter;

public record MovimientoInventarioDto(
    Integer       idMovimiento,
    Integer       productoId,
    String        productoNombre,
    Integer       tallaStockId,      // puede ser null
    String        talla,
    Integer       tipoMovId,
    String        tipoMovNombre,
    BigDecimal    cantidad,          // ya con signo
    String        motivo,
    @JsonbTypeAdapter(OffsetDateTimeAdapter.class)
    OffsetDateTime fechaHora,
    Integer       empleadoId,
    String        empleadoUsuario
) {}

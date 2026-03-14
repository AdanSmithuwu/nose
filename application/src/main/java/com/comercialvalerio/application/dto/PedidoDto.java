package com.comercialvalerio.application.dto;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.comercialvalerio.common.json.OffsetDateTimeAdapter;

import jakarta.json.bind.annotation.JsonbTypeAdapter;

public record PedidoDto(
    Integer       idTransaccion,
    @JsonbTypeAdapter(OffsetDateTimeAdapter.class)
    OffsetDateTime fecha,
    @JsonbTypeAdapter(OffsetDateTimeAdapter.class)
    OffsetDateTime fechaHoraEntrega,
    String        estado,
    BigDecimal    totalNeto,

    /* Campos propios */
    String        direccionEntrega,
    TipoPedido    tipoPedido,
    boolean       usaValeGas,
    String        comentarioCancelacion,

    /* Referencias */
    Integer       empleadoId,
    String        empleadoUsuario,
    Integer       clienteId,
    String        clienteNombre
) {}

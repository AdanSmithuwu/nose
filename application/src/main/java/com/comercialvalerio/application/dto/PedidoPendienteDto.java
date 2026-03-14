package com.comercialvalerio.application.dto;

import java.time.OffsetDateTime;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import com.comercialvalerio.common.json.OffsetDateTimeAdapter;

/** Datos básicos de un pedido pendiente de entrega. */
public record PedidoPendienteDto(
        Integer idTransaccion,
        String clienteNombre,
        @JsonbTypeAdapter(OffsetDateTimeAdapter.class)
        OffsetDateTime fecha
) {}

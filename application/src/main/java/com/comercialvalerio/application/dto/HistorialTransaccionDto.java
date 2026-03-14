package com.comercialvalerio.application.dto;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import com.comercialvalerio.common.json.OffsetDateTimeAdapter;

public record HistorialTransaccionDto(
    Integer       idTransaccion,
    @JsonbTypeAdapter(OffsetDateTimeAdapter.class)
    OffsetDateTime fecha,
    String        tipo,
    BigDecimal    totalNeto,
    BigDecimal    descuento,
    BigDecimal    cargo,
    String        estado
) {}

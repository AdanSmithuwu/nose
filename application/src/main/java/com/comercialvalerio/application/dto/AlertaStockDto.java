package com.comercialvalerio.application.dto;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import com.comercialvalerio.common.json.OffsetDateTimeAdapter;

public record AlertaStockDto(
    Integer idAlerta,
    Integer productoId,
    String  productoNombre,
    BigDecimal stockActual,
    BigDecimal umbral,
    @JsonbTypeAdapter(OffsetDateTimeAdapter.class)
    OffsetDateTime fechaAlerta,
    boolean procesada
) {}

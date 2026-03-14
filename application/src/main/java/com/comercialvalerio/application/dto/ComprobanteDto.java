package com.comercialvalerio.application.dto;
import java.time.OffsetDateTime;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import com.comercialvalerio.common.json.OffsetDateTimeAdapter;

public record ComprobanteDto(
    Integer idComprobante,
    Integer idTransaccion,
    @JsonbTypeAdapter(OffsetDateTimeAdapter.class)
    OffsetDateTime fechaEmision,
    /* PDF codificado en Base-64 para transporte JSON */
    String  pdfBase64
) {}

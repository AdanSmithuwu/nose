package com.comercialvalerio.application.dto;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import com.comercialvalerio.common.json.OffsetDateTimeAdapter;

import com.comercialvalerio.application.dto.TipoReporte;

public record ReporteDto(
    Integer       idReporte,
    TipoReporte   tipoReporte,
    Integer       empleadoId,
    String        empleadoUsuario,
    LocalDate     desde,
    LocalDate     hasta,
    String        filtros,
    byte[]        pdf,               // base64 en el JSON
    @JsonbTypeAdapter(OffsetDateTimeAdapter.class)
    OffsetDateTime fechaGeneracion
) {}

package com.comercialvalerio.application.dto;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import com.comercialvalerio.common.json.OffsetDateTimeAdapter;

import jakarta.json.bind.annotation.JsonbDateFormat;

public record ParametroSistemaDto(
    String clave,
    BigDecimal valor,
    String descripcion,
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
    @JsonbTypeAdapter(OffsetDateTimeAdapter.class)
    OffsetDateTime actualizado,
    Integer idEmpleado,
    String  empleadoUsuario
) {}

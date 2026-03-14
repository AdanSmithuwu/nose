package com.comercialvalerio.application.dto;
import java.time.OffsetDateTime;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import com.comercialvalerio.common.json.OffsetDateTimeAdapter;

public record BitacoraLoginDto(
    Integer       idBitacora,
    Integer       empleadoId,
    String        empleadoUsuario,
    @JsonbTypeAdapter(OffsetDateTimeAdapter.class)
    OffsetDateTime fechaEvento,
    boolean       exitoso
) {}

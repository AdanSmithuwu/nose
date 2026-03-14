package com.comercialvalerio.application.dto;
import jakarta.validation.constraints.NotNull;

public record BitacoraLoginCreateDto(
    @NotNull(message = "idEmpleado obligatorio")
    Integer idEmpleado,
    /* boolean primitivo no necesita validación de null */
    boolean exitoso
) {}

package com.comercialvalerio.application.dto;

import jakarta.json.bind.annotation.JsonbTypeAdapter;
import com.comercialvalerio.common.json.OffsetDateTimeAdapter;

/**
 * Objeto de transferencia de empleados. La serialización JSON la maneja Yasson, por lo que
 * no se requiere un constructor sin argumentos.
 */
public record EmpleadoDto(
    Integer idPersona,
    String nombres,
    String apellidos,
    String dni,
    String telefono,
    Integer idRol,
    String rolNombre,
    String usuario,
    String estado,
    @JsonbTypeAdapter(OffsetDateTimeAdapter.class)
    java.time.OffsetDateTime fechaCambioClave,
    String plainPassword       // solo se devuelve al crear
) {}

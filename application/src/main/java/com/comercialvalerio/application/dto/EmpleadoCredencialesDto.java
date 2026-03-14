package com.comercialvalerio.application.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.comercialvalerio.common.DbConstraints;

/** DTO para actualizar usuario y password de un empleado. */
public record EmpleadoCredencialesDto(
    @Size(max = DbConstraints.LEN_USUARIO)
    @Pattern(regexp = "[\\w.@-]+")
    String usuario,

    /**
     * Nueva contraseña. Si es {@code null} no se modifica la contraseña
     * actual. Si es una cadena vacía se generará automáticamente una
     * nueva contraseña.
     */
    String plainPassword
) {}

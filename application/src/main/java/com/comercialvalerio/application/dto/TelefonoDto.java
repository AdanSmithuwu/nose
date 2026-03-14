package com.comercialvalerio.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.comercialvalerio.common.DbConstraints;

/** DTO simple para recibir un número de teléfono. */
public record TelefonoDto(
    @NotBlank
    @Size(max = DbConstraints.LEN_TELEFONO)
    @Pattern(regexp = "\\d{" + DbConstraints.TEL_MIN_DIGITS + "," + DbConstraints.LEN_TELEFONO + "}")
    String telefono
) {}

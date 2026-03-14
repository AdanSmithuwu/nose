package com.comercialvalerio.application.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.comercialvalerio.common.DbConstraints;

/* DTO para crear o actualizar un Estado. */
public record EstadoCreateDto(
    @NotBlank(message = "El nombre del estado es obligatorio")
    @Size(max = DbConstraints.LEN_NOMBRE_CORTO,
          message = "Máximo " + DbConstraints.LEN_NOMBRE_CORTO + " caracteres")
    @Pattern(regexp = "[^\\p{Cntrl}]+", message = "Carácteres inválidos")
    String nombre,
    @NotBlank(message = "El módulo es obligatorio")
    @Size(max = DbConstraints.LEN_NOMBRE_CORTO,
          message = "Máximo " + DbConstraints.LEN_NOMBRE_CORTO + " caracteres")
    @Pattern(regexp = "[^\\p{Cntrl}]+", message = "Carácteres inválidos")
    String modulo
) {}

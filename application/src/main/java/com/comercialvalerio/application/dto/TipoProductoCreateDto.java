package com.comercialvalerio.application.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.comercialvalerio.common.DbConstraints;

/* DTO para crear o actualizar un Tipo de Producto. */
public record TipoProductoCreateDto(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = DbConstraints.LEN_NOMBRE_CORTO,
          message = "Máximo " + DbConstraints.LEN_NOMBRE_CORTO + " caracteres")
    @Pattern(regexp = "[^\\p{Cntrl}]+", message = "Carácteres inválidos")
    String nombre
) {}

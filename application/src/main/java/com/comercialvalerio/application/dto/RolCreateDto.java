package com.comercialvalerio.application.dto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.comercialvalerio.common.DbConstraints;

/* DTO para crear o actualizar un Rol. */
public record RolCreateDto(
    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = DbConstraints.LEN_NOMBRE_CORTO,
          message = "Máximo " + DbConstraints.LEN_NOMBRE_CORTO + " caracteres")
    @Pattern(regexp = "[^\\p{Cntrl}]+", message = "Carácteres inválidos")
    String nombre,
    @Min(value = 0, message = "El nivel mínimo es 0")
    @Max(value = 9, message = "El nivel máximo es 9")
    short nivel
) {}

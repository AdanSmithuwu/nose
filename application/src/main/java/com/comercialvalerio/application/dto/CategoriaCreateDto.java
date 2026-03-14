package com.comercialvalerio.application.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.comercialvalerio.common.DbConstraints;

/* DTO para crear una Categoría. */
public record CategoriaCreateDto(
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = DbConstraints.LEN_NOMBRE_CATEGORIA,
          message = "El nombre no puede tener más de " + DbConstraints.LEN_NOMBRE_CATEGORIA + " caracteres")
    @Pattern(regexp = "[^\\p{Cntrl}]+", message = "Carácteres inválidos")
    String nombre,
    @Size(max = DbConstraints.LEN_DESCRIPCION,
          message = "La descripción no puede exceder " + DbConstraints.LEN_DESCRIPCION + " caracteres")
    String descripcion
) {}

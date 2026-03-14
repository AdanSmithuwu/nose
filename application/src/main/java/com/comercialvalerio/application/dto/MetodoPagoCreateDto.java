package com.comercialvalerio.application.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.comercialvalerio.common.DbConstraints;

public record MetodoPagoCreateDto(
    @NotBlank(message = "nombre obligatorio")
    @Size(max = DbConstraints.LEN_NOMBRE_CORTO,
          message = "nombre máximo " + DbConstraints.LEN_NOMBRE_CORTO + " caracteres")
    String nombre
) {}

package com.comercialvalerio.application.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.comercialvalerio.common.DbConstraints;

/*
 * Simple DTO para pasar un motivo de cancelación.
 */
public record MotivoDto(
    @NotBlank(message = "motivo obligatorio")
    @Size(max = DbConstraints.LEN_OBSERVACION,
          message = "motivo máximo " + DbConstraints.LEN_OBSERVACION + " caracteres")
    String motivo
) {}

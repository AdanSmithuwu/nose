package com.comercialvalerio.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilizado para cambiar el estado de una entidad.
 */
public record CambiarEstadoDto(@NotBlank String nuevoEstado) {}

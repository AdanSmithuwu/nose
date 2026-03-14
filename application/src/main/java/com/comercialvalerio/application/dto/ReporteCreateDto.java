package com.comercialvalerio.application.dto;
import java.time.LocalDate;

import com.comercialvalerio.common.DbConstraints;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReporteCreateDto(
    @NotNull(message = "tipoReporte obligatorio")
    TipoReporte tipoReporte,
    @NotNull(message = "idEmpleado obligatorio")
    Integer idEmpleado,
    LocalDate desde,
    LocalDate hasta,
    @Size(max = DbConstraints.LEN_FILTROS_REPORTE,
          message = "filtros máximo " + DbConstraints.LEN_FILTROS_REPORTE + " caracteres")
    String filtros,
    @NotNull(message = "pdf obligatorio")
    @Size(min = 1, message = "pdf no puede estar vacío")
    byte[] pdf
) {}

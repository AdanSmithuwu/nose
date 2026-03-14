package com.comercialvalerio.application.dto;
import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.comercialvalerio.common.DbConstraints;

public record ParametroSistemaCreateDto(
    @NotBlank(message = "clave obligatoria")
    @Size(max = DbConstraints.LEN_CLAVE_PARAM,
          message = "clave máximo " + DbConstraints.LEN_CLAVE_PARAM + " caracteres")
    String clave,
    @NotNull(message = "valor obligatorio")
    @Digits(integer = DbConstraints.PRECIO_INTEGER,
            fraction = DbConstraints.PRECIO_SCALE,
            message = "valor debe tener hasta " + (DbConstraints.PRECIO_INTEGER) + " enteros y " + DbConstraints.PRECIO_SCALE + " decimales")
    @DecimalMin(value = "0.00", inclusive = true, message = "valor debe ser positivo")
    BigDecimal valor,
    @Size(max = DbConstraints.LEN_DESCRIPCION,
          message = "descripcion máximo " + DbConstraints.LEN_DESCRIPCION + " caracteres")
    String descripcion,
    @NotNull(message = "idEmpleado obligatorio")
    Integer idEmpleado
) {}

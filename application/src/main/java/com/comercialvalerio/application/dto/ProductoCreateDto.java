package com.comercialvalerio.application.dto;
import java.math.BigDecimal;

import com.comercialvalerio.common.DbConstraints;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear un nuevo producto. Los campos {@code precioUnitario} y
 * {@code stockActual} son opcionales y, de proporcionarse,
 * deben cumplir las restricciones numéricas definidas.
 */
public record ProductoCreateDto(
    @NotBlank(message = "nombre obligatorio")
    @Size(max = DbConstraints.LEN_NOMBRE_PRODUCTO,
          message = "nombre máximo " + DbConstraints.LEN_NOMBRE_PRODUCTO + " caracteres")
    @Pattern(regexp = "[^\\p{Cntrl}]+", message = "Carácteres inválidos")
    String nombre,
    @Size(max = DbConstraints.LEN_DESCRIPCION,
          message = "descripcion máximo " + DbConstraints.LEN_DESCRIPCION + " caracteres")
    String descripcion,
    @NotNull(message = "idCategoria obligatorio")
    Integer idCategoria,
    @NotNull(message = "idTipoProducto obligatorio")
    Integer idTipoProducto,
    @NotBlank(message = "unidadMedida obligatoria")
    @Size(max = DbConstraints.LEN_UNIDAD_MEDIDA,
          message = "unidadMedida máximo " + DbConstraints.LEN_UNIDAD_MEDIDA + " caracteres")
    String unidadMedida,
    @DecimalMin(value = "0.0", inclusive = true, message = "precioUnitario no puede ser negativo")
    @Digits(integer = DbConstraints.PRECIO_INTEGER,
            fraction = DbConstraints.PRECIO_SCALE)
    BigDecimal precioUnitario, // opcional
    boolean mayorista,
    boolean paraPedido,
    @NotNull
    TipoPedido tipoPedidoDefault,
    @Positive(message = "minMayorista debe ser mayor que cero")
    Integer minMayorista,
    @DecimalMin(value = "0.0", inclusive = true, message = "precioMayorista no puede ser negativo")
    @Digits(integer = DbConstraints.PRECIO_INTEGER,
            fraction = DbConstraints.PRECIO_SCALE)
    BigDecimal precioMayorista,
    @DecimalMin(value = "0.0", inclusive = true, message = "stockActual no puede ser negativo")
    @Digits(integer = DbConstraints.STOCK_INTEGER,
            fraction = DbConstraints.STOCK_SCALE)
    BigDecimal stockActual, // opcional
    @NotNull(message = "umbral obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "umbral no puede ser negativo")
    @Digits(integer = DbConstraints.STOCK_INTEGER,
            fraction = DbConstraints.STOCK_SCALE)
    BigDecimal umbral
) {}

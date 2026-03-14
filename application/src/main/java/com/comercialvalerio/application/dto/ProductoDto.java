package com.comercialvalerio.application.dto;
import java.math.BigDecimal;

public record ProductoDto(
    Integer   idProducto,
    String    nombre,
    String    descripcion,

    Integer   categoriaId,
    String    categoriaNombre,

    Integer   tipoProductoId,
    String    tipoProductoNombre,

    String    unidadMedida,
    BigDecimal precioUnitario,

    boolean    mayorista,
    boolean    paraPedido,
    TipoPedido tipoPedidoDefault,
    Integer    minMayorista,
    BigDecimal precioMayorista,

    BigDecimal stockActual,
    BigDecimal umbral,

    String     estado          // «Activo», «Inactivo», …
) {}

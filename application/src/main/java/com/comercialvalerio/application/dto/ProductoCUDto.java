package com.comercialvalerio.application.dto;
import java.math.BigDecimal;
import java.util.List;

import com.comercialvalerio.common.DbConstraints;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/* Entrada/actualización completa de producto con colecciones auxiliares. */
public record ProductoCUDto(
    /* Producto base */
    @NotBlank @Size(max = DbConstraints.LEN_NOMBRE_PRODUCTO)
    @Pattern(regexp = "[^\\p{Cntrl}]+", message = "Carácteres inválidos")
    String nombre,
    @Size(max = DbConstraints.LEN_DESCRIPCION)
    String descripcion,
    @NotNull                  Integer idCategoria,
    @NotNull                  Integer idTipoProducto,
    @NotBlank @Size(max = DbConstraints.LEN_UNIDAD_MEDIDA) String unidadMedida,
    @DecimalMin("0.0")
    @Digits(integer = DbConstraints.PRECIO_INTEGER,
            fraction = DbConstraints.PRECIO_SCALE)
    BigDecimal precioUnitario,
    boolean mayorista,
    boolean paraPedido,
    /* Puede ser nulo si el producto no está disponible para pedido */
    TipoPedido tipoPedidoDefault,
    @Positive                  Integer minMayorista,
    @DecimalMin("0.0")
    @Digits(integer = DbConstraints.PRECIO_INTEGER,
            fraction = DbConstraints.PRECIO_SCALE)
    BigDecimal precioMayorista,
    @DecimalMin("0.0")
    @Digits(integer = DbConstraints.STOCK_INTEGER,
            fraction = DbConstraints.STOCK_SCALE)
    BigDecimal stockActual,
    @NotNull
    @DecimalMin("0.0")
    @Digits(integer = DbConstraints.STOCK_INTEGER,
            fraction = DbConstraints.STOCK_SCALE)
    BigDecimal umbral,

    /* Listas auxiliares */
    List<TallaStockCUDto>   tallas,         // sólo Vestimenta
    List<PresentacionCUDto> presentaciones  // sólo Fraccionable
) {}

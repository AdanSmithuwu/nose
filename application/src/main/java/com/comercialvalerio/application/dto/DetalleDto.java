package com.comercialvalerio.application.dto;
import java.math.BigDecimal;

public record DetalleDto(
    Integer idDetalle,
    Integer idTransaccion,
    Integer idProducto,
    String  productoNombre,
    Integer idTallaStock,   // opcional
    String  talla,
    BigDecimal cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {}

package com.comercialvalerio.application.dto;
import java.math.BigDecimal;
import java.util.List;

public record ProductoVentaDto(
    Integer idProducto,
    String nombre,
    String unidadMedida,
    BigDecimal precioUnitario,
    BigDecimal stockActual,
    List<TallaStockDto> tallas,
    List<PresentacionDto> presentaciones
) {}

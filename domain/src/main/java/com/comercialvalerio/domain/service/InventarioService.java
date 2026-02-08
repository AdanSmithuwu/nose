package com.comercialvalerio.domain.service;

import com.comercialvalerio.domain.model.MovimientoInventario;

/**
 * Servicio de dominio encargado de registrar movimientos de inventario
 * y realizar el ajuste de stock correspondiente.
 */
public interface InventarioService {
    /**
     * Registra un movimiento de inventario (entrada o salida) aplicando
     * el ajuste de stock y generando una {@code AlertaStock} si procede.
     *
     * @param movimiento movimiento a persistir
     * @return movimiento registrado con id asignado
     */
    MovimientoInventario registrarMovimiento(MovimientoInventario movimiento);

    /**
     * Verifica si existe stock suficiente para el producto y talla
     * indicados.
     *
     * @param producto   producto a consultar
     * @param tallaStock talla opcional
     * @param cantidad   cantidad requerida
     * @return {@code true} si hay stock disponible
     */
    boolean tieneStock(com.comercialvalerio.domain.model.Producto producto,
                       com.comercialvalerio.domain.model.TallaStock tallaStock,
                       java.math.BigDecimal cantidad);
}

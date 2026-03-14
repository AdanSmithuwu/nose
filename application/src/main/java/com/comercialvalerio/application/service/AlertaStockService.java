package com.comercialvalerio.application.service;

import java.util.List;

import com.comercialvalerio.application.dto.AlertaStockDto;

public interface AlertaStockService {
    List<AlertaStockDto> listarPendientes();
    void marcarProcesada(Integer idAlerta);
    /** Procesa todas las alertas pendientes del producto y habilita la venta hasta agotar el stock. */
    void procesarProducto(Integer idProducto);
}

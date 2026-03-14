package com.comercialvalerio.application.service;

import java.util.List;

import com.comercialvalerio.application.dto.HistorialDto;

public interface HistorialService {
    List<HistorialDto> historialPorCliente(Integer idCliente);

    /**
     * Historial filtrado por rango de fechas y opcionalmente por
     * categoría o producto.
     */
    List<HistorialDto> historialPorCliente(Integer idCliente,
                                           java.time.LocalDateTime desde,
                                           java.time.LocalDateTime hasta,
                                           Integer idCategoria,
                                           Integer idProducto);
}

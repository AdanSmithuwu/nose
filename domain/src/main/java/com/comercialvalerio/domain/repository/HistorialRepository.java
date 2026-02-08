package com.comercialvalerio.domain.repository;

import java.util.List;

import com.comercialvalerio.domain.view.HistorialTransaccionView;

/**
 * Lectura del historial de transacciones para un cliente.
 */
public interface HistorialRepository {

    /** Obtiene las transacciones asociadas al cliente indicado. */
    List<HistorialTransaccionView> findByCliente(Integer idCliente);

    /**
     * Lista las transacciones del cliente dentro del rango indicado.
     * Si {@code desde} o {@code hasta} son nulos se ignora el filtro.
     */
    List<HistorialTransaccionView> findByCliente(Integer idCliente,
                                                 java.time.LocalDateTime desde,
                                                 java.time.LocalDateTime hasta);
}

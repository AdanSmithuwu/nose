package com.comercialvalerio.domain.repository;

import java.util.List;

import com.comercialvalerio.domain.view.HistorialTransaccionView;

/**
 * Acceso de solo lectura para la vista de historial de transacciones
 * detalladas.
 */
public interface HistorialTransaccionRepository {

    /** Lista todas las transacciones asociadas a un cliente. */
    List<HistorialTransaccionView> findByCliente(Integer idCliente);
}

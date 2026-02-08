package com.comercialvalerio.domain.repository;

import com.comercialvalerio.domain.model.OrdenCompraPdf;

public interface OrdenCompraPdfRepository {
    OrdenCompraPdf findByPedido(Integer idPedido);
    /**
     * Guarda el registro. Si {@code orden} ya tiene un
     * {@code idOrdenCompra} se actualiza en lugar de insertar.
     */
    void save(OrdenCompraPdf orden);
}

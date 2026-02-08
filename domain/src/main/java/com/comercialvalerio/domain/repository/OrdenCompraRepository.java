package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.OrdenCompra;
import java.util.List;

public interface OrdenCompraRepository {
    List<OrdenCompra> findByPedido(Integer idPedido);
    /** Verifica si existen órdenes para el producto indicado. */
    boolean existsByProducto(Integer idProducto);
    void save(OrdenCompra orden);
    void deleteByPedido(Integer idPedido);
}

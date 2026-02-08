package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.DetalleTransaccion;
import java.util.List;

/* Líneas de detalle de ventas/pedidos */
public interface DetalleTransaccionRepository {
    List<DetalleTransaccion> findByTransaccion(Integer idTransaccion);
    /** Verifica si existen detalles para el producto indicado. */
    boolean existsByProducto(Integer idProducto);
    void save(DetalleTransaccion d);
    void delete(Integer idDetalle);
}

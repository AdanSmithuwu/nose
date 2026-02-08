package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.TipoMovimiento;
import java.util.List;

/* Catálogo de tipos de movimiento de inventario. */
public interface TipoMovimientoRepository {
    List<TipoMovimiento> findAll();
    java.util.Optional<TipoMovimiento> findById(Integer id);
    java.util.Optional<TipoMovimiento> findByNombre(String nombre);
    void save(TipoMovimiento tipo);
    void delete(Integer id);
}

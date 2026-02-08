package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.Presentacion;
import java.util.List;

/* Presentaciones (packs, combos) de un producto */
public interface PresentacionRepository {
    List<Presentacion> findByProducto(Integer idProducto);
    /** Obtiene todas las presentaciones por sus ids. */
    List<Presentacion> findAllById(java.util.Collection<Integer> ids);
    List<Presentacion> findByProductos(List<Integer> ids);
    java.util.Optional<Presentacion>       findById(Integer id);
    void               save(Presentacion p);
    void               delete(Integer id);
    void               updateEstado(Integer id, String estado);
    /** Cambia el estado de todas las presentaciones de un producto. */
    void               updateEstadoByProducto(Integer idProducto, String estado);
}

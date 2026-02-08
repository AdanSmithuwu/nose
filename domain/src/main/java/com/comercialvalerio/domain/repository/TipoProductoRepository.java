package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.TipoProducto;
import java.util.List;

public interface TipoProductoRepository {
    List<TipoProducto> findAll();
    java.util.Optional<TipoProducto> findById(Integer id);
    java.util.Optional<TipoProducto> findByNombre(String nombre);
    void save(TipoProducto tipo);
    void delete(Integer id);
}

package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.Estado;
import java.util.List;

public interface EstadoRepository {
    List<Estado> findAll();
    java.util.Optional<Estado> findById(Integer id);
    /* Busca un estado por módulo (p. ej. "Persona", "Producto") y nombre. */
    java.util.Optional<Estado> findByModuloAndNombre(String modulo, String nombre);
    void save(Estado estado);
    void delete(Integer id);
}

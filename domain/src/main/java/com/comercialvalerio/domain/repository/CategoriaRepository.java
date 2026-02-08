package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.Categoria;
import java.util.List;

public interface CategoriaRepository {
    List<Categoria> findAll();
    java.util.Optional<Categoria> findById(Integer id);
    java.util.Optional<Categoria> findByNombre(String nombre);
    /**
     * Verifica si ya existe una categoría con el nombre indicado.
     *
     * @param nombre    nombre a buscar
     * @param excludeId identificador de la categoría a excluir de la
     *                  comparación (puede ser {@code null})
     * @return {@code true} si otra categoría tiene ese nombre
     */
    boolean existsByNombre(String nombre, Integer excludeId);
    void save(Categoria categoria);
    void delete(Integer id);
    int cambiarEstado(Integer id, String nuevoEstado, boolean actualizarProductos);
}

package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.Rol;
import java.util.List;

public interface RolRepository {
    List<Rol> findAll();
    java.util.Optional<Rol> findById(Integer id);
    java.util.Optional<Rol> findByNombre(String nombre);
    void save(Rol rol);
    void delete(Integer id);
}

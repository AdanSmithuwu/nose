package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.MetodoPago;
import java.util.List;

public interface MetodoPagoRepository {
    List<MetodoPago> findAll();
    /** Obtiene todos los métodos por sus ids. */
    List<MetodoPago> findAllById(java.util.Collection<Integer> ids);
    java.util.Optional<MetodoPago> findById(Integer id);
    /* Busca un método de pago por nombre único. */
    java.util.Optional<MetodoPago> findByNombre(String nombre);
    void save(MetodoPago metodo);
    void delete(Integer id);
}

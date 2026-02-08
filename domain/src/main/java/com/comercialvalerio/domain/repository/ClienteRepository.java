package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.Cliente;
import java.util.List;

public interface ClienteRepository {
    List<Cliente> findAll();
    /** Devuelve solo clientes con estado 'Activo'. */
    List<Cliente> findActivos();
    /* Devuelve clientes filtrados por nombre de estado. */
    List<Cliente> findByEstado(String nombre);
    java.util.Optional<Cliente> findById(Integer id);
    java.util.Optional<Cliente> findByDni(String dni);
    /* Busca clientes cuyo nombre o apellido contenga el patrón indicado. */
    List<Cliente> findByNombreLike(String patron);
    /* Busca clientes por coincidencia exacta de teléfono. */
    List<Cliente> findByTelefono(String numero);
    /* Devuelve clientes registrados en los últimos N días. */
    List<Cliente> findByRangoRegistro(java.time.LocalDate desde, java.time.LocalDate hasta);
    void save(Cliente cliente);
    void delete(Integer id);
    void updateEstado(Integer id, String estado);
}

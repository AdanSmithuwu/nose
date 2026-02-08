package com.comercialvalerio.infrastructure.persistence.impl;
import java.util.List;

import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.model.Estado;
import com.comercialvalerio.domain.repository.EstadoRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.EstadoEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.EstadoMapper;
import java.util.Locale;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Implementación JPA de {@link EstadoRepository}. */
@ApplicationScoped
public class EstadoRepositoryImpl extends BaseRepository implements EstadoRepository {
    @Inject
    EstadoMapper mapper;

    private final java.util.concurrent.ConcurrentMap<Integer, Estado> cacheById = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentMap<String, Estado> cacheByKey = new java.util.concurrent.ConcurrentHashMap<>();
    /* --------- Lectura --------- */
    @Override
    public List<Estado> findAll() {
        return read(em -> map(
                em.createNamedQuery("Estado.findAll", EstadoEntity.class),
                mapper::toDomain));
    }
    @Override
    public java.util.Optional<Estado> findById(Integer id) {
        Estado cached = cacheById.get(id);
        if (cached != null) return java.util.Optional.of(cached);
        java.util.Optional<Estado> result = readOptional(em -> {
            EstadoEntity e = em.find(EstadoEntity.class, id);
            return e == null ? null : mapper.toDomain(e);
        });
        result.ifPresent(r -> cacheById.putIfAbsent(id, r));
        return result;
    }
    @Override
    public java.util.Optional<Estado> findByModuloAndNombre(String modulo, String nombre) {
        String key = modulo + "|" + nombre;
        Estado cached = cacheByKey.get(key);
        if (cached != null) return java.util.Optional.of(cached);
        java.util.Optional<Estado> result = readOptional(em -> em.createNamedQuery("Estado.findByModuloAndNombre", EstadoEntity.class)
                            .setParameter("m", modulo)
                            .setParameter("n", nombre)
                            .getResultStream()
                            .findFirst()
                            .map(mapper::toDomain)
                            .orElse(null));
        result.ifPresent(r -> {
            cacheByKey.putIfAbsent(key, r);
            cacheById.putIfAbsent(r.getIdEstado(), r);
        });
        return result;
    }
    /* --------- Escritura --------- */
    @Override
    public void save(Estado estado) {
        tx(em -> {
            /* Unicidad módulo+nombre */
            Long dup = em.createNamedQuery("Estado.countByModuloNombreExcludingId", Long.class)
                .setParameter("m", estado.getModulo().toUpperCase(Locale.ROOT))
                .setParameter("n", estado.getNombre().toUpperCase(Locale.ROOT))
                .setParameter("id", estado.getIdEstado())
                .getSingleResult();
            if (dup > 0)
                throw new DuplicateEntityException("Ya existe el estado «"
                        + estado.getNombre() + "» en módulo «" + estado.getModulo() + "»");

            EstadoEntity ent = mapper.toEntity(estado);
            ent = em.merge(ent);                 // insertar/actualizar
            estado.setIdEstado(ent.getIdEstado());
            return null;
        });
    }
    @Override
    public void delete(Integer id) {
        tx(em -> {
            EstadoEntity e = em.find(EstadoEntity.class, id);
            if (e == null)
                throw new EntityNotFoundException("Estado no encontrado (id=" + id + ")");
            /* Regla de negocio: NO borrar si está referenciado en alguna tabla */
            boolean enUso =
                em.createNamedQuery("Transaccion.countByEstado", Long.class)
                  .setParameter("id", id)
                  .getSingleResult() > 0;
            if (enUso)
                throw new BusinessRuleViolationException("Estado está asociado a transacciones; no se puede eliminar");
            em.remove(e);
            return null;
        });
    }
}

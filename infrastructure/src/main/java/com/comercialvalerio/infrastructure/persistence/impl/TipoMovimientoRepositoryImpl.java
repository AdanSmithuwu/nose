package com.comercialvalerio.infrastructure.persistence.impl;
import java.util.List;

import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.model.TipoMovimiento;
import com.comercialvalerio.domain.repository.TipoMovimientoRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.TipoMovimientoEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.TipoMovimientoMapper;
import java.util.Locale;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Implementación JPA de {@link TipoMovimientoRepository}. */
@ApplicationScoped
public class TipoMovimientoRepositoryImpl extends BaseRepository
                                          implements TipoMovimientoRepository {
@Inject
    TipoMovimientoMapper mapper;
    /* ---------- Lectura ---------- */
    @Override
    public List<TipoMovimiento> findAll() {
        return read(em -> map(
                em.createNamedQuery("TipoMovimiento.findAll", TipoMovimientoEntity.class),
                mapper::toDomain));
    }
    @Override
    public java.util.Optional<TipoMovimiento> findById(Integer id) {
        return readOptional(em -> {
            TipoMovimientoEntity e = em.find(TipoMovimientoEntity.class, id);
            return e == null ? null : mapper.toDomain(e);
        });
    }
    @Override
    public java.util.Optional<TipoMovimiento> findByNombre(String nombre) {
        return readOptional(em -> em.createNamedQuery("TipoMovimiento.findByNombre", TipoMovimientoEntity.class)
                            .setParameter("nombre", nombre)
                            .getResultStream()
                            .findFirst()
                            .map(mapper::toDomain)
                            .orElse(null));
    }
    /* ---------- Escritura ---------- */
    @Override
    public void save(TipoMovimiento tm) {
        tx(em -> {
            /* — Unicidad de nombre — */
            boolean dup =
                em.createNamedQuery("TipoMovimiento.countByNombreExcludingId", Long.class)
                  .setParameter("n", tm.getNombre().toUpperCase(Locale.ROOT))
                  .setParameter("id", tm.getIdTipoMovimiento())
                  .getSingleResult() > 0;

            if (dup)
                throw new DuplicateEntityException(
                    "Ya existe el tipo de movimiento «" + tm.getNombre() + "»");

            TipoMovimientoEntity ent = mapper.toEntity(tm);
            ent = em.merge(ent);                      // insertar / actualizar
            tm.setIdTipoMovimiento(ent.getIdTipoMovimiento());
            return null;
        });
    }
    @Override
    public void delete(Integer id) {
        tx(em -> {
            TipoMovimientoEntity e = em.find(TipoMovimientoEntity.class, id);
            if (e == null)
                throw new EntityNotFoundException("Tipo de movimiento no encontrado (id=" + id + ")");

            /* Regla: no borrar si hay movimientos históricos */
            if (!e.getMovimientos().isEmpty())
                throw new BusinessRuleViolationException(
                    "No se puede eliminar: existen movimientos de inventario con este tipo");

            em.remove(e);
            return null;
        });
    }
}

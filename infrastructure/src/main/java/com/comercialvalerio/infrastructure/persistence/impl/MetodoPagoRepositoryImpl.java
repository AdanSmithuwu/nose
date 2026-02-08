package com.comercialvalerio.infrastructure.persistence.impl;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.model.MetodoPago;
import com.comercialvalerio.domain.repository.MetodoPagoRepository;
import com.comercialvalerio.infrastructure.persistence.CrudRepository;
import com.comercialvalerio.infrastructure.persistence.entity.MetodoPagoEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.MetodoPagoMapper;
import java.util.List;
import java.util.Locale;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Implementación JPA de {@link MetodoPagoRepository}. */
@ApplicationScoped
public class MetodoPagoRepositoryImpl
        extends CrudRepository<MetodoPago, MetodoPagoEntity, Integer>
        implements MetodoPagoRepository {
@Inject
    MetodoPagoMapper mapper;

    public MetodoPagoRepositoryImpl() {
        super(MetodoPagoEntity.class);
    }

    @Override
    public List<MetodoPago> findAllById(java.util.Collection<Integer> ids) {
        return findAllByIds(ids, idField());
    }
    /* -------- Lectura -------- */
    @Override
    public java.util.Optional<MetodoPago> findByNombre(String nombre) {
        return readOptional(em -> em.createNamedQuery("MetodoPago.findByNombre", MetodoPagoEntity.class)
                            .setParameter("nombre", nombre)
                            .getResultStream()
                            .findFirst()
                            .map(mapper::toDomain)
                            .orElse(null));
    }
    /* -------- Escritura -------- */
    @Override
    public void save(MetodoPago mp) {
        tx(em -> {
            /* — Unicidad de nombre — */
            boolean dup = em.createNamedQuery("MetodoPago.countByNombreNotId", Long.class)
                .setParameter("n", mp.getNombre().toUpperCase(Locale.ROOT))
                .setParameter("id", mp.getIdMetodoPago())
                .getSingleResult() > 0;

            if (dup)
                throw new DuplicateEntityException(
                    "Ya existe el método de pago «" + mp.getNombre() + "»");

            MetodoPagoEntity ent = mapper.toEntity(mp);
            ent = em.merge(ent);                 // insertar / actualizar
            mp.setIdMetodoPago(ent.getIdMetodoPago());
            return null;
        });
    }
    @Override
    public void delete(Integer id) {
        tx(em -> {
            MetodoPagoEntity e = em.find(MetodoPagoEntity.class, id);
            if (e == null)
                throw new EntityNotFoundException("Método de pago no encontrado (id=" + id + ")");

            /* No borrar si tiene pagos asociados */
            if (!e.getPagos().isEmpty())
                throw new BusinessRuleViolationException(
                    "No se puede eliminar: existen pagos con este método");

            em.remove(e);
            return null;
        });
    }

    @Override
    protected MetodoPagoEntity toEntity(MetodoPago model) {
        return mapper.toEntity(model);
    }

    @Override
    protected MetodoPago toDomain(MetodoPagoEntity entity) {
        return mapper.toDomain(entity);
    }

    @Override
    protected void assignId(MetodoPago model, MetodoPagoEntity entity) {
        model.setIdMetodoPago(entity.getIdMetodoPago());
    }
}

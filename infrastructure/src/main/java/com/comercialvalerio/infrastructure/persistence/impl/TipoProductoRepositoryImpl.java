package com.comercialvalerio.infrastructure.persistence.impl;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.model.TipoProducto;
import com.comercialvalerio.domain.repository.TipoProductoRepository;
import com.comercialvalerio.infrastructure.persistence.CrudRepository;
import com.comercialvalerio.infrastructure.persistence.entity.TipoProductoEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.TipoProductoMapper;
import java.util.Locale;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Implementación JPA de {@link TipoProductoRepository}. */
@ApplicationScoped
public class TipoProductoRepositoryImpl
        extends CrudRepository<TipoProducto, TipoProductoEntity, Integer>
        implements TipoProductoRepository {
    @Inject
    TipoProductoMapper mapper;

    public TipoProductoRepositoryImpl() {
        super(TipoProductoEntity.class);
    }
    /* ---------- Lectura ---------- */
    @Override
    public java.util.Optional<TipoProducto> findByNombre(String nombre) {
        return readOptional(em -> em.createNamedQuery("TipoProducto.findByNombre", TipoProductoEntity.class)
                            .setParameter("nombre", nombre)
                            .getResultStream()
                            .findFirst()
                            .map(mapper::toDomain)
                            .orElse(null));
    }
    /* ---------- Escritura ---------- */
    @Override
    public void save(TipoProducto tp) {
        tx(em -> {
            /* — Unicidad de nombre — */
            boolean dup =
                em.createNamedQuery("TipoProducto.countByNombreNotId", Long.class)
                  .setParameter("n", tp.getNombre().toUpperCase(Locale.ROOT))
                  .setParameter("id", tp.getIdTipoProducto())
                  .getSingleResult() > 0;

            if (dup)
                throw new DuplicateEntityException(
                    "Ya existe el tipo de producto «" + tp.getNombre() + "»");

            TipoProductoEntity ent = mapper.toEntity(tp);
            ent = em.merge(ent);                 // insertar / actualizar
            tp.setIdTipoProducto(ent.getIdTipoProducto());
            return null;
        });
    }
    @Override
    public void delete(Integer id) {
        tx(em -> {
            TipoProductoEntity e = em.find(TipoProductoEntity.class, id);
            if (e == null)
                throw new EntityNotFoundException("Tipo de producto no encontrado (id=" + id + ")");

            /* Regla negocio: no borrar si hay productos asociados */
            if (!e.getProductos().isEmpty())
                throw new BusinessRuleViolationException(
                    "No se puede eliminar: existen productos con este tipo");

            em.remove(e);
            return null;
        });
    }

    @Override
    protected TipoProductoEntity toEntity(TipoProducto model) {
        return mapper.toEntity(model);
    }

    @Override
    protected TipoProducto toDomain(TipoProductoEntity entity) {
        return mapper.toDomain(entity);
    }

    @Override
    protected void assignId(TipoProducto model, TipoProductoEntity entity) {
        model.setIdTipoProducto(entity.getIdTipoProducto());
    }
}

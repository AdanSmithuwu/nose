package com.comercialvalerio.infrastructure.persistence.impl;
import java.util.List;

import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.model.Presentacion;
import com.comercialvalerio.domain.repository.PresentacionRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.PresentacionEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.PresentacionMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Implementación JPA de {@link PresentacionRepository}. */
@ApplicationScoped
public class PresentacionRepositoryImpl extends BaseRepository implements PresentacionRepository {
@Inject
    PresentacionMapper mapper;
    /* ---------- Lectura ---------- */
    @Override
    public List<Presentacion> findByProducto(Integer idProducto) {
        return read(em -> map(
                em.createNamedQuery("Presentacion.byProducto", PresentacionEntity.class)
                        .setParameter("p", idProducto),
                mapper::toDomain));
    }

    @Override
    public List<Presentacion> findAllById(java.util.Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return read(em -> map(
                em.createNamedQuery("Presentacion.findByIds", PresentacionEntity.class)
                        .setParameter("ids", ids),
                mapper::toDomain));
    }

    @Override
    public List<Presentacion> findByProductos(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return read(em -> map(
                em.createNamedQuery("Presentacion.findByProductos", PresentacionEntity.class)
                        .setParameter("ids", ids),
                mapper::toDomain));
    }
    @Override
    public java.util.Optional<Presentacion> findById(Integer id) {
        return readOptional(em -> {
            PresentacionEntity e = em.find(PresentacionEntity.class, id);
            return e == null ? null : mapper.toDomain(e);
        });
    }
    /* ---------- Escritura ---------- */
    @Override
    public void save(Presentacion pres) {
        tx(em -> {
            PresentacionEntity ent = mapper.toEntity(pres);
            ent.setProducto(em.getReference(
                    com.comercialvalerio.infrastructure.persistence.entity.ProductoEntity.class,
                    pres.getProducto().getIdProducto()));
            ent = em.merge(ent);
            pres.setIdPresentacion(ent.getIdPresentacion());
            return null;
        });
    }
    @Override
    public void delete(Integer idPresentacion) {
        tx(em -> {
            PresentacionEntity e = em.find(PresentacionEntity.class, idPresentacion);
            if (e == null)
                throw new EntityNotFoundException("Presentación no encontrada (id=" + idPresentacion + ")");

            /* Regla de negocio: no borrar si está en DetalleTransaccion */
            boolean enUso = em.createNamedQuery("Presentacion.enUso", Long.class)
                              .setParameter("prod", e.getProducto().getIdProducto())
                              .setParameter("prodEnt", e.getProducto())
                              .getSingleResult() > 0;
            if (enUso)
                throw new BusinessRuleViolationException(
                    "Presentación utilizada en transacciones; no se puede eliminar");

            em.remove(e);
            return null;
        });
    }

    @Override
    public void updateEstado(Integer id, String estado) {
        tx(em -> {
            var nuevo = em.createNamedQuery("Estado.findByModuloAndNombre",
                    com.comercialvalerio.infrastructure.persistence.entity.EstadoEntity.class)
                    .setParameter("m", "Producto")
                    .setParameter("n", estado)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (nuevo == null)
                throw new EntityNotFoundException("Estado no encontrado");

            int n = em.createNamedQuery("Presentacion.updateEstado")
                    .setParameter("e", nuevo)
                    .setParameter("id", id)
                    .executeUpdate();
            if (n == 0)
                throw new EntityNotFoundException("Presentación no encontrada");
            return null;
        });
    }

    @Override
    public void updateEstadoByProducto(Integer idProducto, String estado) {
        tx(em -> {
            var nuevo = em.createNamedQuery("Estado.findByModuloAndNombre",
                    com.comercialvalerio.infrastructure.persistence.entity.EstadoEntity.class)
                    .setParameter("m", "Producto")
                    .setParameter("n", estado)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (nuevo == null)
                throw new EntityNotFoundException("Estado no encontrado");

            em.createNamedQuery("Presentacion.updateEstadoByProducto")
               .setParameter("e", nuevo)
               .setParameter("pId", idProducto)
               .executeUpdate();
            return null;
        });
    }
}

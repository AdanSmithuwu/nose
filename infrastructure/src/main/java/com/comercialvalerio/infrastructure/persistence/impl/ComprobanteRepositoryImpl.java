package com.comercialvalerio.infrastructure.persistence.impl;
import java.time.LocalDateTime;

import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.model.Comprobante;
import com.comercialvalerio.domain.repository.ComprobanteRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.ComprobanteEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.ComprobanteMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ComprobanteRepositoryImpl
        extends BaseRepository implements ComprobanteRepository {
@Inject
    ComprobanteMapper mapper;

    /* -------- Lectura -------- */
    @Override
    public java.util.Optional<Comprobante> findById(Integer id) {
        return readOptional(em -> {
            ComprobanteEntity e = em.find(ComprobanteEntity.class, id);
            return e == null ? null : mapper.toDomain(e);
        });
    }

    @Override
    public java.util.Optional<Comprobante> findByTransaccion(Integer idTx) {
        return readOptional(em -> em.createNamedQuery("Comprobante.byTrans",
                                              ComprobanteEntity.class)
                            .setParameter("id", idTx)
                            .getResultStream()
                            .findFirst()
                            .map(mapper::toDomain)
                            .orElse(null));
    }

    /* -------- Escritura -------- */
    @Override
    public void save(Comprobante c) {
        tx(em -> {

            /* 1) Unicidad */
            boolean dup = em.createNamedQuery(
                                "Comprobante.countByTransaccionExcludingId",
                                Long.class)
                    .setParameter("tx", c.getTransaccion().getIdTransaccion())
                    .setParameter("id", c.getIdComprobante())
                    .getSingleResult() > 0;

            if (dup)
                throw new DuplicateEntityException("La transacción ya tiene comprobante");

            /* 2) PDF no vacío */
            if (c.getBytesPdf() == null || c.getBytesPdf().length == 0)
                throw new BusinessRuleViolationException("bytesPdf vacío");

            ComprobanteEntity e = mapper.toEntity(c);
            e.setTransaccion(em.getReference(
                    com.comercialvalerio.infrastructure.persistence.entity.TransaccionEntity.class,
                    c.getTransaccion().getIdTransaccion()));
            if (e.getFechaEmision() == null)
                e.setFechaEmision(LocalDateTime.now());

            e = em.merge(e);                     // insertar/actualizar
            c.setIdComprobante(e.getIdComprobante());
            c.setFechaEmision(e.getFechaEmision());
            return null;
        });
    }
}

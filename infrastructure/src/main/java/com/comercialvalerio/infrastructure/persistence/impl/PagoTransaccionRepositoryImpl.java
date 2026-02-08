package com.comercialvalerio.infrastructure.persistence.impl;
import java.math.BigDecimal;
import java.util.List;

import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.model.PagoTransaccion;
import com.comercialvalerio.domain.repository.PagoTransaccionRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.PagoTransaccionEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.PagoTransaccionMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PagoTransaccionRepositoryImpl
        extends BaseRepository implements PagoTransaccionRepository {
@Inject
    PagoTransaccionMapper mapper;

    /* ---------- Lectura ---------- */
    @Override
    public List<PagoTransaccion> findByTransaccion(Integer idTransaccion) {
        return read(em -> map(
                em.createNamedQuery("PagoTransaccion.byTrans",
                                   PagoTransaccionEntity.class)
                        .setParameter("id", idTransaccion),
                mapper::toDomain));
    }

    /* ---------- Escritura ---------- */
    @Override
    public void save(PagoTransaccion pago) {
        tx(em -> {

            if (pago.getMonto() == null ||
                pago.getMonto().compareTo(BigDecimal.ZERO) <= 0)
                throw new BusinessRuleViolationException("El monto debe ser > 0");

            long dup = em.createNamedQuery(
                            "PagoTransaccion.countByTransAndMetodo",
                            Long.class)
                    .setParameter("tx", pago.getTransaccion().getIdTransaccion())
                    .setParameter("met", pago.getMetodoPago().getIdMetodoPago())
                    .setParameter("id", pago.getIdPago())
                    .getSingleResult();

            if (dup > 0)
                throw new BusinessRuleViolationException(
                        "Ya existe un pago con ese método para esta transacción");

            PagoTransaccionEntity e = mapper.toEntity(pago);
            e = em.merge(e);                       // insertar / actualizar
            pago.setIdPago(e.getIdPago());
            return null;
        });
    }

    @Override
    public void delete(Integer idPago) {
        tx(em -> {
            PagoTransaccionEntity e =
                    em.find(PagoTransaccionEntity.class, idPago);
            if (e == null)
                throw new EntityNotFoundException("Pago no encontrado (id=" + idPago + ')');
            em.remove(e);
            return null;
        });
    }

    @Override
    public boolean existsByCliente(Integer idCliente) {
        return read(em -> em.createNamedQuery(
                        "PagoTransaccion.countByCliente",
                        Long.class)
                .setParameter("idCli", idCliente)
                .getSingleResult() > 0);
    }
}

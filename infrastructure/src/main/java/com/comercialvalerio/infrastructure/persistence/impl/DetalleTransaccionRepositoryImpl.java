package com.comercialvalerio.infrastructure.persistence.impl;
import java.util.List;

import com.comercialvalerio.domain.model.DetalleTransaccion;
import com.comercialvalerio.domain.repository.DetalleTransaccionRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.DetalleTransaccionEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.DetalleTransaccionMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DetalleTransaccionRepositoryImpl extends BaseRepository implements DetalleTransaccionRepository {
@Inject
    DetalleTransaccionMapper mapper;

    @Override
    public List<DetalleTransaccion> findByTransaccion(Integer idTx) {
        return read(em -> map(
                em.createNamedQuery("DetalleTransaccion.byTrans", DetalleTransaccionEntity.class)
                        .setParameter("id", idTx),
                mapper::toDomain));
    }

    @Override
    public boolean existsByProducto(Integer idProducto) {
        return read(em -> em.createNamedQuery("DetalleTransaccion.countByProducto", Long.class)
                .setParameter("prod", idProducto)
                .getSingleResult() > 0);
    }

    /*
       La BD prohíbe modificar detalles en transacciones cerradas y el
       registro inicial de detalles se hace dentro de los SP de Venta / Pedido.
       Por tanto las operaciones de save / delete se eliminan.
    */
    @Override
    public void save(DetalleTransaccion d) {
        throw new UnsupportedOperationException("Los detalles se insertan únicamente desde los SP");
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Los detalles no pueden eliminarse una vez creada la transacción");
    }
}

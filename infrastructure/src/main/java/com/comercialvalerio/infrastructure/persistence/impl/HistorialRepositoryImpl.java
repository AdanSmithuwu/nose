package com.comercialvalerio.infrastructure.persistence.impl;

import java.util.List;

import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.HistorialTransaccionEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.HistorialTransaccionMapper;
import com.comercialvalerio.domain.repository.HistorialRepository;
import com.comercialvalerio.domain.view.HistorialTransaccionView;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HistorialRepositoryImpl extends BaseRepository
        implements HistorialRepository {

    @jakarta.inject.Inject
    HistorialTransaccionMapper mapper;

    public List<HistorialTransaccionView> findByCliente(Integer idCliente) {
        return read(em -> em.createNamedQuery("HistorialTx.byCliente", HistorialTransaccionEntity.class)
                .setParameter("id", idCliente)
                .getResultList()
                .stream()
                .map(mapper::toDomain)
                .toList());
    }

    public List<HistorialTransaccionView> findByCliente(Integer idCliente,
            java.time.LocalDateTime desde, java.time.LocalDateTime hasta) {
        return read(em -> em.createNamedQuery("HistorialTx.byClienteRange",
                        HistorialTransaccionEntity.class)
                .setParameter("id", idCliente)
                .setParameter("d", desde)
                .setParameter("h", hasta)
                .getResultList()
                .stream()
                .map(mapper::toDomain)
                .toList());
    }
}

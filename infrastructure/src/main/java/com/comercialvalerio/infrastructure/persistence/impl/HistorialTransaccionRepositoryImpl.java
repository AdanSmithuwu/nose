package com.comercialvalerio.infrastructure.persistence.impl;

import java.util.List;

import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.HistorialTransaccionEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.HistorialTransaccionMapper;
import com.comercialvalerio.domain.repository.HistorialTransaccionRepository;
import com.comercialvalerio.domain.view.HistorialTransaccionView;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HistorialTransaccionRepositoryImpl extends BaseRepository
        implements HistorialTransaccionRepository {

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
}

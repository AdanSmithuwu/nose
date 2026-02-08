package com.comercialvalerio.infrastructure.persistence.impl;

import java.util.List;

import com.comercialvalerio.domain.repository.ClienteFrecuenteRepository;
import com.comercialvalerio.domain.view.ClienteFrecuenteView;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.ClienteFrecuenteEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.ClienteFrecuenteMapper;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClienteFrecuenteRepositoryImpl extends BaseRepository
        implements ClienteFrecuenteRepository {

    @jakarta.inject.Inject
    ClienteFrecuenteMapper mapper;

    @Override
    public List<ClienteFrecuenteView> top(int limite) {
        return read(em -> em.createNamedQuery("ClienteFrecuente.top",
                                              ClienteFrecuenteEntity.class)
                             .setParameter(1, limite)
                             .getResultList()
                             .stream()
                             .map(mapper::toDomain)
                             .toList());
    }
}

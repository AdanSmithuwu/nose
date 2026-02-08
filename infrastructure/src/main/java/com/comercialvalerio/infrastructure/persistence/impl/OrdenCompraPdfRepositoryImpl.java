package com.comercialvalerio.infrastructure.persistence.impl;

import com.comercialvalerio.domain.model.OrdenCompraPdf;
import com.comercialvalerio.domain.repository.OrdenCompraPdfRepository;
import com.comercialvalerio.infrastructure.persistence.CrudRepository;
import com.comercialvalerio.infrastructure.persistence.entity.OrdenCompraPdfEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.OrdenCompraPdfMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrdenCompraPdfRepositoryImpl
        extends CrudRepository<OrdenCompraPdf, OrdenCompraPdfEntity, Integer>
        implements OrdenCompraPdfRepository {
    @Inject OrdenCompraPdfMapper mapper;

    public OrdenCompraPdfRepositoryImpl() {
        super(OrdenCompraPdfEntity.class);
    }

    @Override
    public OrdenCompraPdf findByPedido(Integer idPedido) {
        return read(em -> em.createNamedQuery("OrdenCompraPdf.byPedido", OrdenCompraPdfEntity.class)
                .setParameter("idPed", idPedido)
                .getResultStream()
                .findFirst()
                .map(mapper::toDomain)
                .orElse(null));
    }

    @Override
    protected OrdenCompraPdfEntity toEntity(OrdenCompraPdf model) {
        return mapper.toEntity(model);
    }

    @Override
    protected OrdenCompraPdf toDomain(OrdenCompraPdfEntity entity) {
        return mapper.toDomain(entity);
    }

    @Override
    protected void assignId(OrdenCompraPdf model, OrdenCompraPdfEntity entity) {
        model.setIdOrdenCompra(entity.getIdOrdenCompra());
    }
}

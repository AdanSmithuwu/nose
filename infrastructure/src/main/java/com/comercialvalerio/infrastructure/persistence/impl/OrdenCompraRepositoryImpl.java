package com.comercialvalerio.infrastructure.persistence.impl;
import java.util.List;

import com.comercialvalerio.domain.model.OrdenCompra;
import com.comercialvalerio.domain.repository.OrdenCompraRepository;
import com.comercialvalerio.infrastructure.persistence.CrudRepository;
import com.comercialvalerio.infrastructure.persistence.entity.OrdenCompraEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.OrdenCompraMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrdenCompraRepositoryImpl
        extends CrudRepository<OrdenCompra, OrdenCompraEntity, Integer>
        implements OrdenCompraRepository {
    @Inject OrdenCompraMapper mapper;

    public OrdenCompraRepositoryImpl() {
        super(OrdenCompraEntity.class);
    }

    @Override
    public List<OrdenCompra> findByPedido(Integer idPedido) {
        return read(em -> map(
                em.createNamedQuery("OrdenCompra.byPedido", OrdenCompraEntity.class)
                        .setParameter("idPed", idPedido),
                mapper::toDomain));
    }

    @Override
    public boolean existsByProducto(Integer idProducto) {
        return read(em -> em.createNamedQuery(
                        "OrdenCompra.countByProducto",
                        Long.class)
                .setParameter("idProd", idProducto)
                .getSingleResult() > 0);
    }

    @Override
    public void deleteByPedido(Integer idPedido) {
        tx(em -> {
            em.createNamedQuery("OrdenCompra.deleteByPedido")
                    .setParameter("idPed", idPedido)
                    .executeUpdate();
            return null;
        });
    }

    @Override
    protected OrdenCompraEntity toEntity(OrdenCompra model) {
        return mapper.toEntity(model);
    }

    @Override
    protected OrdenCompra toDomain(OrdenCompraEntity entity) {
        return mapper.toDomain(entity);
    }

    @Override
    protected void assignId(OrdenCompra model, OrdenCompraEntity entity) {
        model.setIdOrdenCompra(entity.getIdOrdenCompra());
    }
}

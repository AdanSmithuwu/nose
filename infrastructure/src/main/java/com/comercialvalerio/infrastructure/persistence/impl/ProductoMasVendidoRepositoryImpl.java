package com.comercialvalerio.infrastructure.persistence.impl;

import java.util.List;

import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.ProductoMasVendidoEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.ProductoMasVendidoMapper;
import com.comercialvalerio.domain.repository.ProductoMasVendidoRepository;
import com.comercialvalerio.domain.view.ProductoMasVendido;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductoMasVendidoRepositoryImpl extends BaseRepository
        implements ProductoMasVendidoRepository {

    @jakarta.inject.Inject
    ProductoMasVendidoMapper mapper;

    public List<ProductoMasVendido> top(int limite) {
        return read(em -> em.createNamedQuery("ProductoMasVendido.top", ProductoMasVendidoEntity.class)
                             .setMaxResults(limite)
                             .getResultList()
                             .stream()
                             .map(mapper::toDomain)
                             .toList());
    }
}

package com.comercialvalerio.infrastructure.persistence.impl;

import java.math.BigDecimal;

import com.comercialvalerio.domain.repository.DashboardRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DashboardRepositoryImpl extends BaseRepository implements DashboardRepository {

    @Override
    public BigDecimal totalVentas() {
        return read(em -> em.createNamedQuery(
                "Transaccion.totalVentasCompletadas", BigDecimal.class)
                .getSingleResult());
    }

    @Override
    public long totalPedidos() {
        return read(em -> em.createNamedQuery(
                "Pedido.totalPedidos", Long.class)
                .getSingleResult());
    }
}

package com.comercialvalerio.domain.repository;
import java.math.BigDecimal;

public interface DashboardRepository {
    BigDecimal totalVentas();
    long totalPedidos();
}

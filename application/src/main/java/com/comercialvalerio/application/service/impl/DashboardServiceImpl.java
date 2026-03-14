package com.comercialvalerio.application.service.impl;

import com.comercialvalerio.application.dto.DashboardMetricasDto;
import com.comercialvalerio.application.dto.ClienteFrecuenteDto;
import com.comercialvalerio.application.mapper.ClienteFrecuenteDtoMapper;
import com.comercialvalerio.application.service.DashboardService;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.repository.DashboardRepository;
import com.comercialvalerio.domain.repository.ClienteFrecuenteRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository repo;
    private final ClienteFrecuenteRepository repoCli;
    @Inject ClienteFrecuenteDtoMapper mapper;

    @Inject
    public DashboardServiceImpl(DashboardRepository repo,
                               ClienteFrecuenteRepository repoCli) {
        this.repo = repo;
        this.repoCli = repoCli;
    }

    @Override
    public DashboardMetricasDto indicadores() {
        return new DashboardMetricasDto(repo.totalVentas(), repo.totalPedidos());
    }

    @Override
    public java.util.List<ClienteFrecuenteDto> clientesFrecuentes(int limite) {
        return ServiceUtils.mapList(repoCli.top(limite), mapper::toDto);
    }
}

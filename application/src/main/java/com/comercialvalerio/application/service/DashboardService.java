package com.comercialvalerio.application.service;

import com.comercialvalerio.application.dto.DashboardMetricasDto;
import com.comercialvalerio.application.dto.ClienteFrecuenteDto;
import java.util.List;

public interface DashboardService {
    DashboardMetricasDto indicadores();
    List<ClienteFrecuenteDto> clientesFrecuentes(int limite);
}

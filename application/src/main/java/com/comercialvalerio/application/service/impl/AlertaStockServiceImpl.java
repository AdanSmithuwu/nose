package com.comercialvalerio.application.service.impl;

import java.util.List;

import com.comercialvalerio.application.dto.AlertaStockDto;
import com.comercialvalerio.application.mapper.AlertaStockDtoMapper;
import com.comercialvalerio.application.service.AlertaStockService;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.notification.NotificadorAlertaStock;
import com.comercialvalerio.domain.repository.AlertaStockRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class AlertaStockServiceImpl implements AlertaStockService {

    private final AlertaStockRepository repo;
    private final NotificadorAlertaStock notificador;
    private final com.comercialvalerio.domain.repository.ProductoRepository prodRepo;
    @Inject AlertaStockDtoMapper mapper;

    @Inject
    public AlertaStockServiceImpl(AlertaStockRepository repo,
                                  NotificadorAlertaStock notificador,
                                  com.comercialvalerio.domain.repository.ProductoRepository prodRepo) {
        this.repo = repo;
        this.notificador = notificador;
        this.prodRepo = prodRepo;
    }

    @Override
    public List<AlertaStockDto> listarPendientes() {
        var lista = repo.findPendientes();
        return ServiceUtils.mapList(lista, mapper::toDto);
    }

    @Override
    public void marcarProcesada(Integer idAlerta) {
        repo.marcarProcesada(idAlerta);
    }

    @Override
    public void procesarProducto(Integer idProducto) {
        repo.marcarProcesadaByProducto(idProducto);
        prodRepo.updateEstado(idProducto, com.comercialvalerio.domain.model.EstadoNombre.ACTIVO.getNombre());
        prodRepo.setIgnorarUmbralHastaCero(idProducto, true);
    }
}

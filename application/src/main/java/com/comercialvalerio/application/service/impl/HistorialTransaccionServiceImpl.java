package com.comercialvalerio.application.service.impl;

import java.util.List;

import com.comercialvalerio.application.dto.HistorialTransaccionDto;
import com.comercialvalerio.application.service.HistorialTransaccionService;
import com.comercialvalerio.common.time.DateMapper;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.repository.HistorialTransaccionRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class HistorialTransaccionServiceImpl implements HistorialTransaccionService {
    private final HistorialTransaccionRepository repo;

    @Inject
    public HistorialTransaccionServiceImpl(HistorialTransaccionRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<HistorialTransaccionDto> listarPorCliente(Integer idCliente) {
        return repo.findByCliente(idCliente).stream()
                   .map(e -> new HistorialTransaccionDto(
                           e.idTransaccion(),
                           DateMapper.toOffsetDateTime(e.fecha()),
                           e.tipo(),
                           e.totalNeto(),
                           e.descuento(),
                           e.cargo(),
                           e.estado()))
                   .toList();
    }
}

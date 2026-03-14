package com.comercialvalerio.application.service;

import java.util.List;

import com.comercialvalerio.application.dto.HistorialTransaccionDto;

public interface HistorialTransaccionService {
    List<HistorialTransaccionDto> listarPorCliente(Integer idCliente);
}

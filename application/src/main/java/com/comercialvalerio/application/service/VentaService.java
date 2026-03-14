package com.comercialvalerio.application.service;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.application.dto.VentaCreateDto;
import com.comercialvalerio.application.dto.VentaDto;
import com.comercialvalerio.application.dto.MotivoDto;

public interface VentaService {
    List<VentaDto> listar();
    List<VentaDto> listarPorRango(LocalDateTime d, LocalDateTime h);
    List<VentaDto> listarPorCliente(Integer idCliente);
    VentaDto       obtener(Integer id);
    VentaDto       crear(VentaCreateDto dto);
    void           cancelar(Integer idVenta, MotivoDto motivo);
}

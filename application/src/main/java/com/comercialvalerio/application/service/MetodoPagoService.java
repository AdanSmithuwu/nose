package com.comercialvalerio.application.service;
import java.util.List;

import com.comercialvalerio.application.dto.MetodoPagoCreateDto;
import com.comercialvalerio.application.dto.MetodoPagoDto;

public interface MetodoPagoService {
    List<MetodoPagoDto> listar();
    MetodoPagoDto       obtener(Integer id);
    MetodoPagoDto       buscarPorNombre(String nombre);
    MetodoPagoDto       crear(MetodoPagoCreateDto dto);
    MetodoPagoDto       actualizar(Integer id, MetodoPagoCreateDto dto);
    void                eliminar(Integer id);
}

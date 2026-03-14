package com.comercialvalerio.application.service;
import java.util.List;

import com.comercialvalerio.application.dto.TipoMovimientoCreateDto;
import com.comercialvalerio.application.dto.TipoMovimientoDto;

public interface TipoMovimientoService {
    List<TipoMovimientoDto> listar();
    TipoMovimientoDto       obtener(Integer id);
    TipoMovimientoDto       buscarPorNombre(String nombre);
    TipoMovimientoDto       crear(TipoMovimientoCreateDto dto);
    TipoMovimientoDto       actualizar(Integer id, TipoMovimientoCreateDto dto);
    void                    eliminar(Integer id);
}

package com.comercialvalerio.application.service;
import java.util.List;

import com.comercialvalerio.application.dto.TipoProductoCreateDto;
import com.comercialvalerio.application.dto.TipoProductoDto;

public interface TipoProductoService {
    List<TipoProductoDto> listar();
    TipoProductoDto       obtener(Integer id);
    TipoProductoDto       buscarPorNombre(String nombre);
    TipoProductoDto       crear(TipoProductoCreateDto dto);
    TipoProductoDto       actualizar(Integer id, TipoProductoCreateDto dto);
    void                  eliminar(Integer id);
}

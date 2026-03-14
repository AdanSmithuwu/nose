package com.comercialvalerio.application.service;
import java.util.List;

import com.comercialvalerio.application.dto.EstadoCreateDto;
import com.comercialvalerio.application.dto.EstadoDto;

public interface EstadoService {
    List<EstadoDto> listar();
    EstadoDto obtener(Integer id);
    EstadoDto crear(EstadoCreateDto dto);
    EstadoDto actualizar(Integer id, EstadoCreateDto dto);
    void      eliminar(Integer id);
    EstadoDto      buscarPorModuloYNombre(String modulo, String nombre);
}

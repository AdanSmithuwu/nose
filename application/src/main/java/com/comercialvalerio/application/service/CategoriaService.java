package com.comercialvalerio.application.service;
import java.util.List;

import com.comercialvalerio.application.dto.CategoriaCreateDto;
import com.comercialvalerio.application.dto.CategoriaDto;
import com.comercialvalerio.application.dto.CambiarEstadoDto;

public interface CategoriaService {
    List<CategoriaDto> listar();
    CategoriaDto obtener(Integer id);
    CategoriaDto crear(CategoriaCreateDto dto);
    CategoriaDto actualizar(Integer id, CategoriaCreateDto dto);
    void          eliminar(Integer id);
    int           cambiarEstado(Integer id, CambiarEstadoDto dto, boolean actualizarProductos);
    /** Devuelve las entidades que impiden eliminar la categoría. */
    List<String> obtenerDependencias(Integer idCategoria);
}

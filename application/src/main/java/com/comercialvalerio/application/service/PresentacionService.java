package com.comercialvalerio.application.service;
import java.util.List;

import com.comercialvalerio.application.dto.PresentacionCreateDto;
import com.comercialvalerio.application.dto.PresentacionDto;

public interface PresentacionService {
    List<PresentacionDto> listarPorProducto(Integer idProducto);
    PresentacionDto      obtener(Integer id);
    PresentacionDto      crear(PresentacionCreateDto dto);
    PresentacionDto      actualizar(Integer id, PresentacionCreateDto dto);
    void                 eliminar(Integer id);
    void                 activar(Integer id);
    void                 desactivar(Integer id);
    /** Lista todas las presentaciones, incluyendo inactivas. */
    List<PresentacionDto> listarTodosPorProducto(Integer idProducto);
}

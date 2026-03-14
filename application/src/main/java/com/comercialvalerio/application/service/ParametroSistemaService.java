package com.comercialvalerio.application.service;
import java.util.List;

import com.comercialvalerio.application.dto.ParametroSistemaCreateDto;
import com.comercialvalerio.application.dto.ParametroSistemaDto;

public interface ParametroSistemaService {
    List<ParametroSistemaDto> listar();
    ParametroSistemaDto       obtener(String clave);
    /** Actualiza un parámetro existente. */
    ParametroSistemaDto       guardar(String clave, ParametroSistemaCreateDto dto);
}

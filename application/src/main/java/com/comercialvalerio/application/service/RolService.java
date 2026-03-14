package com.comercialvalerio.application.service;
import java.util.List;

import com.comercialvalerio.application.dto.RolDto;

public interface RolService {
    List<RolDto> listar();
    RolDto obtener(Integer id);
    RolDto buscarPorNombre(String nombre);
}

package com.comercialvalerio.application.service;
import java.util.List;

import com.comercialvalerio.application.dto.DetalleDto;

public interface DetalleService {
    List<DetalleDto> listar(Integer idTransaccion);
}

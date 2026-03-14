package com.comercialvalerio.application.service;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.application.dto.MovimientoInventarioCreateDto;
import com.comercialvalerio.application.dto.MovimientoInventarioDto;

public interface MovimientoInventarioService {
    List<MovimientoInventarioDto> listarPorProducto(Integer idProducto);
    List<MovimientoInventarioDto> listarPorRango(LocalDateTime d, LocalDateTime h);
    MovimientoInventarioDto       registrar(MovimientoInventarioCreateDto dto);
}

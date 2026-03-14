package com.comercialvalerio.application.service;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.application.dto.BitacoraLoginCreateDto;
import com.comercialvalerio.application.dto.BitacoraLoginDto;

public interface BitacoraLoginService {
    List<BitacoraLoginDto> listarPorEmpleado(Integer idEmpleado);
    List<BitacoraLoginDto> listarPorRango(LocalDateTime desde, LocalDateTime hasta,
                                          Boolean resultado);
    BitacoraLoginDto      registrar(BitacoraLoginCreateDto dto);
    void                  depurarAntiguos();
}

package com.comercialvalerio.application.service;
import java.util.List;

import com.comercialvalerio.application.dto.PagoCreateDto;
import com.comercialvalerio.application.dto.PagoDto;

public interface PagoService {
    List<PagoDto> listar(Integer idTransaccion);
    PagoDto       registrar(Integer idTransaccion, PagoCreateDto dto);
    void eliminar(Integer idTx, Integer idPago);
}

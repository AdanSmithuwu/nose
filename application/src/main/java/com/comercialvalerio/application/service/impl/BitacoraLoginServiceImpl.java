package com.comercialvalerio.application.service.impl;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.application.dto.BitacoraLoginCreateDto;
import com.comercialvalerio.application.dto.BitacoraLoginDto;
import com.comercialvalerio.application.mapper.BitacoraLoginDtoMapper;
import com.comercialvalerio.application.service.BitacoraLoginService;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.repository.BitacoraLoginRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class BitacoraLoginServiceImpl implements BitacoraLoginService {

    private final BitacoraLoginRepository repoBit;
    @Inject
    BitacoraLoginDtoMapper mapper;

    @Inject
    public BitacoraLoginServiceImpl(BitacoraLoginRepository repoBit) {
        this.repoBit = repoBit;
    }

    @Override public List<BitacoraLoginDto> listarPorEmpleado(Integer idEmp) {
        repoBit.depurarAntiguos(LocalDateTime.now().minusYears(1));
        return ServiceUtils.mapList(repoBit.findByEmpleado(idEmp), mapper::toDto);
    }

    @Override public List<BitacoraLoginDto> listarPorRango(LocalDateTime d,
                                                           LocalDateTime h,
                                                           Boolean resultado) {
        if (d.isAfter(h))
            throw new BusinessRuleViolationException("El rango 'desde' no puede ser posterior a 'hasta'");
        repoBit.depurarAntiguos(LocalDateTime.now().minusYears(1));
        return ServiceUtils.mapList(
                repoBit.findByRangoFecha(d, h, resultado), mapper::toDto);
    }

    @Override public BitacoraLoginDto registrar(BitacoraLoginCreateDto dto) {
        throw new BusinessRuleViolationException(
                "Los intentos de login se registran automáticamente");
    }

    @Override
    public void depurarAntiguos() {
        repoBit.depurarAntiguos(LocalDateTime.now().minusYears(1));
    }
}

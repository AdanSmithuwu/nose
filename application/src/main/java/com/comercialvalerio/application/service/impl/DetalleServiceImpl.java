package com.comercialvalerio.application.service.impl;
import java.util.List;

import com.comercialvalerio.application.dto.DetalleDto;
import com.comercialvalerio.application.mapper.DetalleDtoMapper;
import com.comercialvalerio.application.service.DetalleService;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.repository.DetalleTransaccionRepository;
import com.comercialvalerio.application.service.util.ServiceUtils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class DetalleServiceImpl implements DetalleService {

    private final DetalleTransaccionRepository repoDet;
@Inject
    DetalleDtoMapper mapper;

    @Inject
    public DetalleServiceImpl(DetalleTransaccionRepository repoDet) {
        this.repoDet = repoDet;
    }

    @Override
    public List<DetalleDto> listar(Integer idTx) {
        return ServiceUtils.mapList(
                repoDet.findByTransaccion(idTx),
                mapper::toDto);
    }

    /* agregar() / eliminar() desaparecen – la interfaz DetalleService se
       reduce a solo lectura.  Asegúrate de actualizarla en el dominio. */
}

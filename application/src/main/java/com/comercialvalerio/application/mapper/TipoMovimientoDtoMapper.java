package com.comercialvalerio.application.mapper;
import org.mapstruct.Mapper;

import com.comercialvalerio.application.dto.TipoMovimientoDto;
import com.comercialvalerio.domain.model.TipoMovimiento;

@Mapper(componentModel = "cdi")
public interface TipoMovimientoDtoMapper {
    TipoMovimientoDto toDto(TipoMovimiento model);
}

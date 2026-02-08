package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.domain.model.TipoMovimiento;
import com.comercialvalerio.infrastructure.persistence.entity.TipoMovimientoEntity;

@Mapper(componentModel = "cdi")
public interface TipoMovimientoMapper {
    TipoMovimiento toDomain(TipoMovimientoEntity entidad);
    @Mapping(target="movimientos", ignore=true)
    TipoMovimientoEntity toEntity(TipoMovimiento modelo);
}

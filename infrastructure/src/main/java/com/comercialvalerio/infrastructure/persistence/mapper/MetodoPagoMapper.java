package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.domain.model.MetodoPago;
import com.comercialvalerio.infrastructure.persistence.entity.MetodoPagoEntity;

@Mapper(componentModel = "cdi")
public interface MetodoPagoMapper {
    MetodoPago toDomain(MetodoPagoEntity entidad);
    @Mapping(target="pagos", ignore=true)
    MetodoPagoEntity toEntity(MetodoPago modelo);
}

package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.domain.model.DetalleTransaccion;
import com.comercialvalerio.infrastructure.persistence.entity.DetalleTransaccionEntity;

@Mapper(componentModel = "cdi", uses = {TransaccionMapper.class, ProductoMapper.class, TallaStockMapper.class})
public interface DetalleTransaccionMapper {
    @Mapping(target = "transaccion", ignore = true)
    DetalleTransaccion toDomain(DetalleTransaccionEntity entidad);
    @Mapping(target = "transaccion", ignore = true)
    DetalleTransaccionEntity toEntity(DetalleTransaccion modelo);
}

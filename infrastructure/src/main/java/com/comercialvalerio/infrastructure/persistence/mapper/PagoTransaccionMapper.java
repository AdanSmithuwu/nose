package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.domain.model.PagoTransaccion;
import com.comercialvalerio.infrastructure.persistence.entity.PagoTransaccionEntity;

@Mapper(componentModel = "cdi", uses = {TransaccionMapper.class, MetodoPagoMapper.class})
public interface PagoTransaccionMapper {
    @Mapping(target = "transaccion", ignore = true)
    PagoTransaccion toDomain(PagoTransaccionEntity entidad);
    @Mapping(target = "transaccion", ignore = true)
    PagoTransaccionEntity toEntity(PagoTransaccion modelo);
}

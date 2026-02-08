package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.domain.model.Comprobante;
import com.comercialvalerio.infrastructure.persistence.entity.ComprobanteEntity;

@Mapper(componentModel = "cdi", uses = {TransaccionMapper.class})
public interface ComprobanteMapper {
    @Mapping(target = "transaccion", ignore = true)
    Comprobante toDomain(ComprobanteEntity entidad);
    @Mapping(target = "transaccion", ignore = true)
    ComprobanteEntity toEntity(Comprobante modelo);
}

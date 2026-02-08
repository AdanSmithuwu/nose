package com.comercialvalerio.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;

import com.comercialvalerio.domain.model.OrdenCompraPdf;
import com.comercialvalerio.infrastructure.persistence.entity.OrdenCompraPdfEntity;

@Mapper(componentModel = "cdi", uses = {PedidoMapper.class})
public interface OrdenCompraPdfMapper {
    OrdenCompraPdf toDomain(OrdenCompraPdfEntity entidad);
    OrdenCompraPdfEntity toEntity(OrdenCompraPdf modelo);
}

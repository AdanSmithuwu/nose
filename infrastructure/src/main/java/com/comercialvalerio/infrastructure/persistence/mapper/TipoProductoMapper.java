package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.domain.model.TipoProducto;
import com.comercialvalerio.infrastructure.persistence.entity.TipoProductoEntity;

@Mapper(componentModel = "cdi")
public interface TipoProductoMapper {
    TipoProducto toDomain(TipoProductoEntity entidad);
    @Mapping(target = "productos", ignore = true)
    TipoProductoEntity toEntity(TipoProducto modelo);
}

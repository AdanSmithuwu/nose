package com.comercialvalerio.application.mapper;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.application.dto.TipoProductoCreateDto;
import com.comercialvalerio.application.dto.TipoProductoDto;
import com.comercialvalerio.domain.model.TipoProducto;

@Mapper(componentModel = "cdi")
public interface TipoProductoDtoMapper {

    TipoProductoDto toDto(TipoProducto model);

    @InheritInverseConfiguration
    @Mapping(target = "idTipoProducto", ignore = true)
    TipoProducto toModel(TipoProductoCreateDto dto);
}

package com.comercialvalerio.application.mapper;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.application.dto.EstadoCreateDto;
import com.comercialvalerio.application.dto.EstadoDto;
import com.comercialvalerio.domain.model.Estado;

@Mapper(componentModel = "cdi")
public interface EstadoDtoMapper {
    EstadoDto toDto(Estado model);
    @InheritInverseConfiguration
    @Mapping(target = "idEstado", ignore = true)
    Estado toModel(EstadoCreateDto dto);
}

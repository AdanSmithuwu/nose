package com.comercialvalerio.application.mapper;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.application.dto.RolCreateDto;
import com.comercialvalerio.application.dto.RolDto;
import com.comercialvalerio.domain.model.Rol;

@Mapper(componentModel = "cdi")
public interface RolDtoMapper {
    RolDto toDto(Rol model);
    @InheritInverseConfiguration
    @Mapping(target = "idRol", ignore = true)
    Rol toModel(RolCreateDto dto);
}

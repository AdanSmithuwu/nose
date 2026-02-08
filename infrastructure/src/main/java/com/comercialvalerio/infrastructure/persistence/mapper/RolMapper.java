package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.domain.model.Rol;
import com.comercialvalerio.infrastructure.persistence.entity.RolEntity;

@Mapper(componentModel = "cdi")
public interface RolMapper {
    Rol toDomain(RolEntity entidad);
    @Mapping(target="empleadoCollection", ignore=true)
    RolEntity toEntity(Rol modelo);
}

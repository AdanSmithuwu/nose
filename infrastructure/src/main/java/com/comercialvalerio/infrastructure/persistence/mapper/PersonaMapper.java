package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.domain.model.Persona;
import com.comercialvalerio.infrastructure.persistence.entity.PersonaEntity;

@Mapper(componentModel = "cdi", uses = {EstadoMapper.class})
public interface PersonaMapper {
    Persona toDomain(PersonaEntity entidad);
    @Mapping(target="empleado", ignore=true)
    @Mapping(target="cliente", ignore=true)
    PersonaEntity toEntity(Persona modelo);
}

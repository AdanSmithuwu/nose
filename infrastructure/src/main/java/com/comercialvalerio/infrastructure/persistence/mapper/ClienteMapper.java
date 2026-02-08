package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.domain.model.Cliente;
import com.comercialvalerio.infrastructure.persistence.entity.ClienteEntity;

@Mapper(componentModel = "cdi", uses = {PersonaMapper.class})
public interface ClienteMapper {
    /*
     * 1) MapStruct verá el property 'persona' en ClienteEntity,
     * 2) Delegará a PersonaMapper.toDomain(entidad.getPersona()),
     * 3) Y 'aplanará' todos esos campos sobre el objeto Cliente.
     */
    @Mapping(source = "persona", target = ".")
    @Mapping(source = "persona.estado", target = "estado")
    Cliente toDomain(ClienteEntity entidad);
    @Mapping(target = "persona", ignore = true)
    @Mapping(target = "transacciones", ignore = true)
    ClienteEntity toEntity(Cliente modelo);
}

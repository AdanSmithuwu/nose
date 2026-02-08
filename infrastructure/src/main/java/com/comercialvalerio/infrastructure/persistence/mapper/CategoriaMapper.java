package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.domain.model.Categoria;
import com.comercialvalerio.infrastructure.persistence.entity.CategoriaEntity;

@Mapper(componentModel = "cdi", uses = EstadoMapper.class)
public interface CategoriaMapper {
    Categoria toDomain(CategoriaEntity entidad);
    @Mapping(target="productos", ignore=true)
    CategoriaEntity toEntity(Categoria modelo);
}

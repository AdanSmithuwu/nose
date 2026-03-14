package com.comercialvalerio.application.mapper;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.application.dto.CategoriaCreateDto;
import com.comercialvalerio.application.dto.CategoriaDto;
import com.comercialvalerio.domain.model.Categoria;

@Mapper(componentModel = "cdi")
public interface CategoriaDtoMapper {

    @Mapping(source = "estado.nombre", target = "estado")
    CategoriaDto toDto(Categoria model);

    @InheritInverseConfiguration(name = "toDto")
    @Mapping(target = "idCategoria", ignore = true)
    @Mapping(target = "estado", ignore = true)
    Categoria toModel(CategoriaCreateDto dto);
}

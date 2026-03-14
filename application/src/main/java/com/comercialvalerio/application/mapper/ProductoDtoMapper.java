package com.comercialvalerio.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.domain.model.Producto;

@Mapper(componentModel = "cdi")
public interface ProductoDtoMapper {

    @Mappings({
        @Mapping(source = "categoria.idCategoria",   target = "categoriaId"),
        @Mapping(source = "categoria.nombre",        target = "categoriaNombre"),
        @Mapping(source = "tipoProducto.idTipoProducto", target = "tipoProductoId"),
        @Mapping(source = "tipoProducto.nombre",     target = "tipoProductoNombre"),
        @Mapping(source = "estado.nombre",           target = "estado")
    })
    ProductoDto toDto(Producto model);
}

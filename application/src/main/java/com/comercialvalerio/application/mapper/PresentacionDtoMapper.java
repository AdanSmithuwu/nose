package com.comercialvalerio.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.comercialvalerio.application.dto.PresentacionDto;
import com.comercialvalerio.domain.model.Presentacion;

@Mapper(componentModel = "cdi")
public interface PresentacionDtoMapper {

    @Mappings({
        @Mapping(source = "producto.idProducto",  target = "productoId"),
        @Mapping(source = "producto.nombre",      target = "productoNombre"),
        @Mapping(source = "estado.nombre",        target = "estado")
    })
    PresentacionDto toDto(Presentacion model);
}

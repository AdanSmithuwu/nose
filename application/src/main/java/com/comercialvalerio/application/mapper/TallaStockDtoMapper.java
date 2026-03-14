package com.comercialvalerio.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.comercialvalerio.application.dto.TallaStockDto;
import com.comercialvalerio.domain.model.TallaStock;

@Mapper(componentModel = "cdi")
public interface TallaStockDtoMapper {

    @Mappings({
        @Mapping(source = "producto.idProducto",  target = "productoId"),
        @Mapping(source = "producto.nombre",      target = "productoNombre"),
        @Mapping(source = "estado.nombre",        target = "estado")
    })
    TallaStockDto toDto(TallaStock model);
}

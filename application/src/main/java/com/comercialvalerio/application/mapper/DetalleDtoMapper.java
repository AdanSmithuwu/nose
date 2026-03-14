package com.comercialvalerio.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.comercialvalerio.application.dto.DetalleDto;
import com.comercialvalerio.domain.model.DetalleTransaccion;

@Mapper(componentModel = "cdi")
public interface DetalleDtoMapper {

    @Mappings({
        @Mapping(source="idDetalle",                target="idDetalle"),
        @Mapping(source="transaccion.idTransaccion",target="idTransaccion"),
        @Mapping(source="producto.idProducto",      target="idProducto"),
        @Mapping(source="producto.nombre",          target="productoNombre"),
        @Mapping(source="tallaStock.idTallaStock",  target="idTallaStock"),
        @Mapping(source="tallaStock.talla",         target="talla"),
    })
    DetalleDto toDto(DetalleTransaccion d);
}

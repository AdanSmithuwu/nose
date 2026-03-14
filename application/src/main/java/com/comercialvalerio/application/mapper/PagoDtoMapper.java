package com.comercialvalerio.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.comercialvalerio.application.dto.PagoDto;
import com.comercialvalerio.domain.model.PagoTransaccion;

@Mapper(componentModel = "cdi")
public interface PagoDtoMapper {

    @Mappings({
        @Mapping(source="idPago",                   target="idPago"),
        @Mapping(source="transaccion.idTransaccion",target="idTransaccion"),
        @Mapping(source="metodoPago.idMetodoPago",  target="idMetodoPago"),
        @Mapping(source="metodoPago.nombre",        target="metodoNombre")
    })
    PagoDto toDto(PagoTransaccion p);
}

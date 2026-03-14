package com.comercialvalerio.application.mapper;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.application.dto.MetodoPagoCreateDto;
import com.comercialvalerio.application.dto.MetodoPagoDto;
import com.comercialvalerio.domain.model.MetodoPago;

@Mapper(componentModel = "cdi")
public interface MetodoPagoDtoMapper {
    MetodoPagoDto toDto(MetodoPago model);
    @InheritInverseConfiguration
    @Mapping(target = "idMetodoPago", ignore = true)
    MetodoPago toModel(MetodoPagoCreateDto dto);
}

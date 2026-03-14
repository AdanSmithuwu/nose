package com.comercialvalerio.application.mapper;

import org.mapstruct.Mapper;

import com.comercialvalerio.application.dto.ProductoMasVendidoDto;
import com.comercialvalerio.domain.view.ProductoMasVendido;

@Mapper(componentModel = "cdi")
public interface ProductoMasVendidoDtoMapper {
    ProductoMasVendidoDto toDto(ProductoMasVendido model);
}

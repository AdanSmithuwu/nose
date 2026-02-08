package com.comercialvalerio.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;

import com.comercialvalerio.domain.view.ProductoMasVendido;
import com.comercialvalerio.infrastructure.persistence.entity.ProductoMasVendidoEntity;

@Mapper(componentModel = "cdi")
public interface ProductoMasVendidoMapper {
    ProductoMasVendido toDomain(ProductoMasVendidoEntity entity);
}

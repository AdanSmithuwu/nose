package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.domain.model.TallaStock;
import com.comercialvalerio.infrastructure.persistence.entity.TallaStockEntity;

@Mapper(componentModel = "cdi", uses = {ProductoMapper.class, EstadoMapper.class})
public interface TallaStockMapper {
    /*
     * De forma similar a PresentacionMapper, mapear el Producto asociado puede
     * regresar a este mapper a través de las colecciones del producto.
     * Se ignoran dichas colecciones para evitar un StackOverflowError.
     */
    @Mapping(target = "producto.presentaciones", ignore = true)
    @Mapping(target = "producto.tallas", ignore = true)
    TallaStock toDomain(TallaStockEntity entidad);
    @Mapping(target = "producto", ignore = true)
    @Mapping(target="movimientos", ignore=true)
    @Mapping(target="detalles", ignore=true)
    TallaStockEntity toEntity(TallaStock modelo);
}

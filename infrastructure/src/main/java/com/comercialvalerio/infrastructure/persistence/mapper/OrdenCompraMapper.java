package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.domain.model.OrdenCompra;
import com.comercialvalerio.infrastructure.persistence.entity.OrdenCompraEntity;

@Mapper(componentModel = "cdi", uses = {PedidoMapper.class, ClienteMapper.class, ProductoMapper.class})
public interface OrdenCompraMapper {
    @Mapping(source = "pedido", target = "pedido")
    OrdenCompra toDomain(OrdenCompraEntity entidad);
    OrdenCompraEntity toEntity(OrdenCompra modelo);
}

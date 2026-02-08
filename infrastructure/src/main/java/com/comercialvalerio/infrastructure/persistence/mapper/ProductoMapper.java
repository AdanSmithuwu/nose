package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

import com.comercialvalerio.domain.model.Producto;
import com.comercialvalerio.domain.model.TipoPedido;
import com.comercialvalerio.infrastructure.persistence.entity.ProductoEntity;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi",
        uses = {CategoriaMapper.class, EstadoMapper.class, TipoProductoMapper.class,
                TallaStockMapper.class, PresentacionMapper.class})
public interface ProductoMapper {
    @Mapping(target = "tipoPedidoDefault", source = "tipoPedidoDefault")
    @Mapping(target = "mayorista", ignore = true)
    @Mapping(target = "minMayorista", ignore = true)
    @Mapping(target = "precioMayorista", ignore = true)
    Producto toDomain(ProductoEntity entidad);

    @Mapping(target = "tipoPedidoDefault", source = "tipoPedidoDefault")
    ProductoEntity toEntity(Producto modelo);

    @AfterMapping
    default void mapMayorista(ProductoEntity entidad, @MappingTarget Producto target) {
        if (entidad == null) return;
        target.setMayorista(entidad.getMayorista(),
                entidad.getMinMayorista(),
                entidad.getPrecioMayorista());
    }

    default TipoPedido map(String nombre) {
        return nombre == null ? null
                : TipoPedido.fromNombre(nombre);
    }

    default String map(TipoPedido tipo) {
        return tipo == null ? null : tipo.getNombre();
    }
}

package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.domain.model.Presentacion;
import com.comercialvalerio.infrastructure.persistence.entity.PresentacionEntity;

@Mapper(componentModel = "cdi", uses = {ProductoMapper.class, EstadoMapper.class})
public interface PresentacionMapper {
    /*
     * Al convertir una entidad al modelo de dominio también se mapea el
     * producto asociado. Ese producto contiene a su vez la lista de
     * presentaciones que invocarían nuevamente a este mapper generando
     * recursión infinita. Ignorar las colecciones del producto rompe el
     * ciclo manteniendo los datos básicos del mismo.
     */
    @Mapping(target = "producto.presentaciones", ignore = true)
    @Mapping(target = "producto.tallas", ignore = true)
    Presentacion toDomain(PresentacionEntity entidad);

    @Mapping(target = "producto", ignore = true)
    PresentacionEntity toEntity(Presentacion modelo);
}

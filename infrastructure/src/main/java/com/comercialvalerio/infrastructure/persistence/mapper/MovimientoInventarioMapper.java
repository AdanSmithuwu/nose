package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;

import com.comercialvalerio.domain.model.MovimientoInventario;
import com.comercialvalerio.infrastructure.persistence.entity.MovimientoInventarioEntity;

@Mapper(componentModel = "cdi", uses = {EmpleadoMapper.class, ProductoMapper.class, TallaStockMapper.class, TipoMovimientoMapper.class})
public interface MovimientoInventarioMapper {
    MovimientoInventario toDomain(MovimientoInventarioEntity entidad);
    MovimientoInventarioEntity toEntity(MovimientoInventario modelo);
}

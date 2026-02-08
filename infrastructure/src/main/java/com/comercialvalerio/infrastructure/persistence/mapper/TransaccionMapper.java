package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

import com.comercialvalerio.domain.model.Transaccion;
import com.comercialvalerio.infrastructure.persistence.entity.TransaccionEntity;

@Mapper(componentModel = "cdi",
        uses = {EstadoMapper.class,
                EmpleadoMapper.class,
                ClienteMapper.class,
                DetalleTransaccionMapper.class,
                PagoTransaccionMapper.class})
public interface TransaccionMapper {
    Transaccion toDomain(TransaccionEntity entidad);
    @Mapping(target = "detalles", ignore = true)
    @Mapping(target = "pagos"   , ignore = true)
    @Mapping(target = "pedido"  , ignore = true)
    @Mapping(target = "venta"   , ignore = true)
    @Mapping(target = "comprobante", ignore = true)
    TransaccionEntity toEntity(Transaccion modelo);

    @AfterMapping
    default void mapTotales(TransaccionEntity entidad,
                            @MappingTarget Transaccion target) {
        if (entidad != null) {
            target.setTotales(entidad.getTotalBruto(), entidad.getDescuento(),
                             entidad.getCargo(), entidad.getTotalNeto());
        }
    }
}

package com.comercialvalerio.infrastructure.persistence.mapper;
import com.comercialvalerio.domain.model.Venta;
import com.comercialvalerio.infrastructure.persistence.entity.VentaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi",
        uses = {TransaccionMapper.class,
                EmpleadoMapper.class,
                ClienteMapper.class,
                EstadoMapper.class,
                DetalleTransaccionMapper.class,
                PagoTransaccionMapper.class})
public interface VentaMapper {

    @Mapping(target = "idTransaccion",   source = "transaccion.idTransaccion")
    @Mapping(target = "fecha",           source = "transaccion.fecha")
    @Mapping(target = "estado",          source = "transaccion.estado")
    @Mapping(target = "observacion",     source = "transaccion.observacion")
    @Mapping(target = "motivoCancelacion", source = "transaccion.motivoCancelacion")
    @Mapping(target = "empleado",        source = "transaccion.empleado")
    @Mapping(target = "cliente",         source = "transaccion.cliente")
    @Mapping(target = "detalles",        ignore = true)
    @Mapping(target = "pagos",           ignore = true)
    Venta toDomain(VentaEntity entidad);

    @AfterMapping
    default void mapTotales(VentaEntity entidad, @MappingTarget Venta target) {
        if (entidad != null && entidad.getTransaccion() != null) {
            var t = entidad.getTransaccion();
            target.setTotales(t.getTotalBruto(), t.getDescuento(),
                             t.getCargo(), t.getTotalNeto());
        }
    }

    @Mapping(source = ".", target = "transaccion")
    @Mapping(source = "idTransaccion", target = "idTransaccion")
    VentaEntity toEntity(Venta modelo);
}

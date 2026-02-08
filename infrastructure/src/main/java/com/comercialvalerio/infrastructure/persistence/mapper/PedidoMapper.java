package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

import com.comercialvalerio.domain.model.Pedido;
import com.comercialvalerio.domain.model.TipoPedido;
import com.comercialvalerio.infrastructure.persistence.entity.PedidoEntity;

@Mapper(componentModel = "cdi",
        uses = {TransaccionMapper.class,
                EmpleadoMapper.class,
                ClienteMapper.class,
                EstadoMapper.class,
                DetalleTransaccionMapper.class,
                PagoTransaccionMapper.class})
public interface PedidoMapper {

    @Mapping(target = "idTransaccion",   source = "transaccion.idTransaccion")
    @Mapping(target = "fecha",           source = "transaccion.fecha")
    @Mapping(target = "estado",          source = "transaccion.estado")
    @Mapping(target = "observacion",     source = "transaccion.observacion")
    @Mapping(target = "motivoCancelacion", source = "transaccion.motivoCancelacion")
    @Mapping(target = "empleado",        source = "transaccion.empleado")
    @Mapping(target = "cliente",         source = "transaccion.cliente")
    @Mapping(target = "detalles",        ignore = true)
    @Mapping(target = "pagos",           ignore = true)
    @Mapping(target = "fechaHoraEntrega",   source = "fechaHoraEntrega")
    @Mapping(target = "idEmpleadoEntrega",  ignore = true)
    @Mapping(target = "comentarioCancelacion", ignore = true)
    Pedido toDomain(PedidoEntity entidad);

    @AfterMapping
    default void mapTotales(PedidoEntity entidad, @MappingTarget Pedido target) {
        if (entidad == null || entidad.getTransaccion() == null) {
            return;
        }
        var t = entidad.getTransaccion();
        if (t.getTotalBruto() != null && t.getDescuento() != null
                && t.getCargo() != null && t.getTotalNeto() != null) {
            target.setTotales(t.getTotalBruto(), t.getDescuento(),
                             t.getCargo(), t.getTotalNeto());
        }
    }

    @AfterMapping
    default void mapEmpleadoEntrega(PedidoEntity entidad,
                                   @MappingTarget Pedido target) {
        if (entidad != null && entidad.getEmpleadoEntrega() != null) {
            target.setIdEmpleadoEntrega(entidad.getEmpleadoEntrega().getIdPersona());
        }
    }

    @AfterMapping
    default void mapComentarioCancelacion(PedidoEntity entidad,
                                          @MappingTarget Pedido target) {
        if (entidad == null) {
            return;
        }
        var estado = target.getEstado();
        if (estado != null && com.comercialvalerio.domain.model.EstadoNombre.CANCELADA.equalsNombre(estado.getNombre())) {
            target.setComentarioCancelacion(entidad.getComentarioCancelacion());
        }
    }

    @Mapping(source = ".", target = "transaccion")
    @Mapping(source = "idTransaccion", target = "idTransaccion")
    @Mapping(target = "fechaHoraEntrega",   source = "fechaHoraEntrega")
    @Mapping(target = "empleadoEntrega",    source = "idEmpleadoEntrega")
    PedidoEntity toEntity(Pedido modelo);

    default TipoPedido map(String nombre) {
        return nombre == null ? null
                : TipoPedido.fromNombre(nombre);
    }

    default String map(TipoPedido tipo) {
        return tipo == null ? null : tipo.getNombre();
    }
    default com.comercialvalerio.infrastructure.persistence.entity.EmpleadoEntity map(Integer id) {
        return id == null ? null
                : new com.comercialvalerio.infrastructure.persistence.entity.EmpleadoEntity(id);
    }
}

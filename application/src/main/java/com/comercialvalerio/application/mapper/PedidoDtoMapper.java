package com.comercialvalerio.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import com.comercialvalerio.common.time.DateMapper;

import com.comercialvalerio.application.dto.PedidoDto;
import com.comercialvalerio.domain.model.Pedido;

@Mapper(componentModel = "cdi")
public interface PedidoDtoMapper {

    @Mappings({
        @Mapping(source="idTransaccion",     target="idTransaccion"),
        @Mapping(source="fecha",             target="fecha"),
        @Mapping(source="fechaHoraEntrega",  target="fechaHoraEntrega"),
        @Mapping(source="estado.nombre",     target="estado"),
        @Mapping(source="totalNeto",         target="totalNeto"),
        @Mapping(source="direccionEntrega",  target="direccionEntrega"),
        @Mapping(source="tipoPedido",        target="tipoPedido"),
        @Mapping(source="usaValeGas",        target="usaValeGas"),
        @Mapping(source="comentarioCancelacion", target="comentarioCancelacion"),
        @Mapping(source="empleado.idPersona",    target="empleadoId"),
        @Mapping(source="empleado.usuario",      target="empleadoUsuario"),
        @Mapping(source="cliente.idPersona",     target="clienteId"),
        @Mapping(source="cliente.nombres",       target="clienteNombre")
    })
    PedidoDto toDto(Pedido p);

    /** Convierte {@link LocalDateTime} del dominio en un {@link OffsetDateTime} usando
     *  la zona horaria configurada para preservar el offset al cruzar el límite REST. */
    default OffsetDateTime map(LocalDateTime fecha) {
        return DateMapper.toOffsetDateTime(fecha);
    }
}

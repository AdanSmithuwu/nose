package com.comercialvalerio.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import com.comercialvalerio.common.time.DateMapper;

import com.comercialvalerio.application.dto.MovimientoInventarioDto;
import com.comercialvalerio.domain.model.MovimientoInventario;

@Mapper(componentModel = "cdi")
public interface MovimientoInventarioDtoMapper {

    @Mappings({
        @Mapping(source="producto.idProducto",            target="productoId"),
        @Mapping(source="producto.nombre",                target="productoNombre"),
        @Mapping(source="tallaStock.idTallaStock",        target="tallaStockId"),
        @Mapping(source="tallaStock.talla",               target="talla"),
        @Mapping(source="tipoMovimiento.idTipoMovimiento",target="tipoMovId"),
        @Mapping(source="tipoMovimiento.nombre",          target="tipoMovNombre"),
        @Mapping(source="empleado.idPersona",             target="empleadoId"),
        @Mapping(source="empleado.usuario",               target="empleadoUsuario"),
        @Mapping(source="cantidad",                       target="cantidad"),
        @Mapping(source="fechaHora",                      target="fechaHora")
    })
    MovimientoInventarioDto toDto(MovimientoInventario m);

    /** Convierte {@link LocalDateTime} del dominio en un {@link OffsetDateTime} usando
     *  la zona horaria configurada. */
    default OffsetDateTime mapFechaHora(LocalDateTime fecha) {
        return DateMapper.toOffsetDateTime(fecha);
    }
}

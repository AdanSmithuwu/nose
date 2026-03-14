package com.comercialvalerio.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import com.comercialvalerio.common.time.DateMapper;

import com.comercialvalerio.application.dto.AlertaStockDto;
import com.comercialvalerio.domain.model.AlertaStock;

@Mapper(componentModel = "cdi")
public interface AlertaStockDtoMapper {
    @Mapping(target="productoId", source="producto.idProducto")
    @Mapping(target="productoNombre", source="producto.nombre")
    @Mapping(source="fechaAlerta", target="fechaAlerta")
    AlertaStockDto toDto(AlertaStock model);

    /** Convierte {@link LocalDateTime} del dominio en un {@link OffsetDateTime} usando
     *  la zona horaria configurada. */
    default OffsetDateTime mapFechaAlerta(LocalDateTime fecha) {
        return DateMapper.toOffsetDateTime(fecha);
    }
}

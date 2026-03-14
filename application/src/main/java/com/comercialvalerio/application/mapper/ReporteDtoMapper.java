package com.comercialvalerio.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import com.comercialvalerio.common.time.DateMapper;

import com.comercialvalerio.application.dto.ReporteDto;
import com.comercialvalerio.domain.model.Reporte;

@Mapper(componentModel = "cdi")
public interface ReporteDtoMapper {

    @Mappings({
        @Mapping(source="empleado.idPersona", target="empleadoId"),
        @Mapping(source="empleado.usuario",   target="empleadoUsuario"),
        @Mapping(source="bytesPdf",           target="pdf")
    })
    ReporteDto toDto(Reporte r);

    /** Convierte {@link LocalDateTime} del dominio en un {@link OffsetDateTime} usando
     *  la zona horaria configurada para preservar el offset al cruzar el límite REST. */
    default OffsetDateTime mapFechaGeneracion(LocalDateTime fecha) {
        return DateMapper.toOffsetDateTime(fecha);
    }
}

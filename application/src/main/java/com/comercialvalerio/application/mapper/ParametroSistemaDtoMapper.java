package com.comercialvalerio.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import com.comercialvalerio.common.time.DateMapper;

import com.comercialvalerio.application.dto.ParametroSistemaDto;
import com.comercialvalerio.domain.model.ParametroSistema;

@Mapper(componentModel = "cdi")
public interface ParametroSistemaDtoMapper {

    @Mappings({
        @Mapping(source = "empleado.idPersona", target = "idEmpleado"),
        @Mapping(source = "empleado.usuario",   target = "empleadoUsuario")
    })
    ParametroSistemaDto toDto(ParametroSistema model);

    /** Convierte {@link LocalDateTime} a {@link OffsetDateTime} usando la zona configurada. */
    default OffsetDateTime mapActualizado(LocalDateTime fecha) {
        return DateMapper.toOffsetDateTime(fecha);
    }
}

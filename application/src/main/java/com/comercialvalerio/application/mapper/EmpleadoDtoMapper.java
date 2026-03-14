package com.comercialvalerio.application.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import com.comercialvalerio.common.time.DateMapper;

import com.comercialvalerio.application.dto.EmpleadoDto;
import com.comercialvalerio.domain.model.Empleado;

@Mapper(componentModel = "cdi")
public interface EmpleadoDtoMapper {

    @Mappings({
        @Mapping(source = "rol.idRol",     target = "idRol"),
        @Mapping(source = "rol.nombre",    target = "rolNombre"),
        @Mapping(source = "estado.nombre", target = "estado"),
        @Mapping(source = "fechaCambioClave", target = "fechaCambioClave"),
        @Mapping(target = "plainPassword", ignore = true)
    })
    EmpleadoDto toDto(Empleado model);

    /** Convierte {@link LocalDateTime} del dominio en un {@link OffsetDateTime} usando
     * la zona horaria configurada. */
    default OffsetDateTime mapFechaCambioClave(LocalDateTime fecha) {
        return DateMapper.toOffsetDateTime(fecha);
    }
}

package com.comercialvalerio.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import com.comercialvalerio.common.time.DateMapper;

import com.comercialvalerio.application.dto.BitacoraLoginDto;
import com.comercialvalerio.domain.model.BitacoraLogin;

@Mapper(componentModel = "cdi")
public interface BitacoraLoginDtoMapper {

    @Mappings({
        @Mapping(source = "empleado.idPersona", target = "empleadoId"),
        @Mapping(source = "empleado.usuario",   target = "empleadoUsuario"),
        @Mapping(source = "fechaEvento",        target = "fechaEvento")
    })
    BitacoraLoginDto toDto(BitacoraLogin model);

    /** Convierte un {@link LocalDateTime} en un {@link OffsetDateTime} usando
     *  la zona horaria configurada. */
    default OffsetDateTime mapFechaEvento(LocalDateTime fecha) {
        return DateMapper.toOffsetDateTime(fecha);
    }
}

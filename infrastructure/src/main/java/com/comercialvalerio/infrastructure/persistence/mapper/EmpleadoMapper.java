package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.time.LocalDateTime;

import com.comercialvalerio.domain.model.Empleado;
import com.comercialvalerio.infrastructure.persistence.entity.EmpleadoEntity;

@Mapper(componentModel = "cdi", uses = {PersonaMapper.class, RolMapper.class})
public interface EmpleadoMapper {
    /*
     * Igual que en Cliente: aplana primero 'persona' y luego 
     * asigna el rol con RolMapper.
     */
    @Mapping(source = "persona", target = ".")
    @Mapping(source = "persona.estado", target = "estado")
    @Mapping(target = "fechaCambioClave", qualifiedByName = "sanitizeFechaCambio")
    @Mapping(target = "bloqueadoHasta", qualifiedByName = "sanitizeBloqueadoHasta")
    Empleado toDomain(EmpleadoEntity entidad);
    /*
     * Nuevamente, la vinculación a PersonaEntity la deja 
     * para la capa de repositorio (o service). 
     */
    @Mapping(target = "persona", ignore = true)
    @Mapping(target = "movimientos", ignore = true)
    @Mapping(target = "reportes", ignore = true)
    @Mapping(target = "transacciones", ignore = true)
    @Mapping(target = "parametros", ignore = true)
    @Mapping(target = "bitacoras", ignore = true)
    @Mapping(target = "fechaCambioClave", source = "fechaCambioClave")
    EmpleadoEntity toEntity(Empleado modelo);

    @Named("sanitizeBloqueadoHasta")
    default LocalDateTime sanitizeBloqueadoHasta(LocalDateTime value) {
        return value != null && value.isBefore(LocalDateTime.now()) ? null : value;
    }

    @Named("sanitizeFechaCambio")
    default LocalDateTime sanitizeFechaCambio(LocalDateTime value) {
        return value != null ? value : LocalDateTime.now();
    }
}

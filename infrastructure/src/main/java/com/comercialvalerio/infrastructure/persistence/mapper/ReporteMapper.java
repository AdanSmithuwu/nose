package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;

import com.comercialvalerio.domain.model.Reporte;
import com.comercialvalerio.infrastructure.persistence.entity.ReporteEntity;
import com.comercialvalerio.domain.model.TipoReporte;

@Mapper(componentModel = "cdi", uses = {EmpleadoMapper.class})
public interface ReporteMapper {
    Reporte toDomain(ReporteEntity entidad);
    ReporteEntity toEntity(Reporte modelo);

    default TipoReporte map(String nombre) {
        return nombre == null ? null
                : TipoReporte.fromNombre(nombre);
    }

    default String map(TipoReporte tipo) {
        return tipo == null ? null : tipo.getNombre();
    }
}

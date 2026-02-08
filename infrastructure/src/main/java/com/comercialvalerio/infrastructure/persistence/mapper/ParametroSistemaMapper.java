package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;

import com.comercialvalerio.domain.model.ParametroSistema;
import com.comercialvalerio.infrastructure.persistence.entity.ParametroSistemaEntity;

@Mapper(componentModel = "cdi", uses = {EmpleadoMapper.class})
public interface ParametroSistemaMapper {
    ParametroSistema toDomain(ParametroSistemaEntity entidad);
    ParametroSistemaEntity toEntity(ParametroSistema modelo);
}

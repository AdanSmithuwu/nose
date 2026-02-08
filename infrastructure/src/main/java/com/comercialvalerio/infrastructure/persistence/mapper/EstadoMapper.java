package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;

import com.comercialvalerio.domain.model.Estado;
import com.comercialvalerio.infrastructure.persistence.entity.EstadoEntity;

@Mapper(componentModel = "cdi")
public interface EstadoMapper {
    Estado toDomain(EstadoEntity entidad);
    EstadoEntity toEntity(Estado modelo);
}

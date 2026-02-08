package com.comercialvalerio.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;

import com.comercialvalerio.domain.view.HistorialTransaccionView;
import com.comercialvalerio.infrastructure.persistence.entity.HistorialTransaccionEntity;

@Mapper(componentModel = "cdi")
public interface HistorialTransaccionMapper {
    HistorialTransaccionView toDomain(HistorialTransaccionEntity entity);
}

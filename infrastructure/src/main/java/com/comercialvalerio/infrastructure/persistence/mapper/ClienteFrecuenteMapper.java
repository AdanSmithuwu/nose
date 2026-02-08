package com.comercialvalerio.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;

import com.comercialvalerio.domain.view.ClienteFrecuenteView;
import com.comercialvalerio.infrastructure.persistence.entity.ClienteFrecuenteEntity;

@Mapper(componentModel = "cdi")
public interface ClienteFrecuenteMapper {
    ClienteFrecuenteView toDomain(ClienteFrecuenteEntity entity);
}

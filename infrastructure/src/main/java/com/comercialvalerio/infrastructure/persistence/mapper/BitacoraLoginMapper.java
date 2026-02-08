package com.comercialvalerio.infrastructure.persistence.mapper;
import org.mapstruct.Mapper;

import com.comercialvalerio.domain.model.BitacoraLogin;
import com.comercialvalerio.infrastructure.persistence.entity.BitacoraLoginEntity;

@Mapper(componentModel = "cdi", uses = {EmpleadoMapper.class})
public interface BitacoraLoginMapper {
    BitacoraLogin toDomain(BitacoraLoginEntity entidad);
    BitacoraLoginEntity toEntity(BitacoraLogin modelo);
}

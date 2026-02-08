package com.comercialvalerio.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;

import com.comercialvalerio.domain.model.AlertaStock;
import com.comercialvalerio.infrastructure.persistence.entity.AlertaStockEntity;

@Mapper(componentModel = "cdi", uses = {ProductoMapper.class})
public interface AlertaStockMapper {
    AlertaStock toDomain(AlertaStockEntity entity);
    AlertaStockEntity toEntity(AlertaStock model);
}

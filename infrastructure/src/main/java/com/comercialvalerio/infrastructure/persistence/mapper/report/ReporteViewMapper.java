package com.comercialvalerio.infrastructure.persistence.mapper.report;

import org.mapstruct.Mapper;

import com.comercialvalerio.domain.view.report.*;
import com.comercialvalerio.infrastructure.persistence.entity.report.*;

@Mapper(componentModel = "cdi")
public interface ReporteViewMapper {
    TransaccionesDia toDomain(TransaccionesDiaEntity e);
    PagoMetodoDia toDomain(PagoMetodoDiaEntity e);
    ResumenModalidad toDomain(ResumenModalidadEntity e);
    RotacionProducto toDomain(RotacionProductoEntity e);
    ResumenCategoria toDomain(ResumenCategoriaEntity e);
}

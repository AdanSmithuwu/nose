package com.comercialvalerio.application.mapper;

import org.mapstruct.Mapper;
import com.comercialvalerio.application.dto.ClienteFrecuenteDto;
import com.comercialvalerio.domain.view.ClienteFrecuenteView;

@Mapper(componentModel = "cdi")
public interface ClienteFrecuenteDtoMapper {
    ClienteFrecuenteDto toDto(ClienteFrecuenteView entity);
}

package com.comercialvalerio.application.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.comercialvalerio.application.dto.ClienteCreateDto;
import com.comercialvalerio.application.dto.ClienteDto;
import com.comercialvalerio.domain.model.Cliente;

@Mapper(componentModel = "cdi")
public interface ClienteDtoMapper {

    @Mappings({
        @Mapping(source = "estado.nombre", target = "estado"),
        @Mapping(target = "nombreCompleto",
                 expression = "java(model.getNombres() + \" \" + model.getApellidos())")
    })
    ClienteDto toDto(Cliente model);

    /* Para registrar / actualizar */
    @InheritInverseConfiguration(name = "toDto")
    @Mapping(target = "idPersona",     ignore = true)
    @Mapping(target = "estado",        ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    Cliente toModel(ClienteCreateDto dto);
}

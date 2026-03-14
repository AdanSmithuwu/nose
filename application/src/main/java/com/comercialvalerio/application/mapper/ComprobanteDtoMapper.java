package com.comercialvalerio.application.mapper;
import java.util.Base64;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import com.comercialvalerio.common.time.DateMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.comercialvalerio.application.dto.ComprobanteDto;
import com.comercialvalerio.domain.model.Comprobante;

@Mapper(
  componentModel = "cdi",
  imports = { Base64.class }    // ← aquí
)
public interface ComprobanteDtoMapper {

    @Mappings({
      @Mapping(source="idComprobante",             target="idComprobante"),
      @Mapping(target="idTransaccion",
               expression="java(c.getTransaccion() == null ? null : c.getTransaccion().getIdTransaccion())"),
      @Mapping(source="fechaEmision",              target="fechaEmision"),
      @Mapping(target="pdfBase64",
               expression="java(Base64.getEncoder().encodeToString(c.getBytesPdf()))")
    })
    ComprobanteDto toDto(Comprobante c);

    /** Convierte {@link LocalDateTime} del dominio en un {@link OffsetDateTime} usando
     * la zona horaria configurada para preservar el offset a través del límite REST. */
    default OffsetDateTime mapFechaEmision(LocalDateTime fecha) {
        return DateMapper.toOffsetDateTime(fecha);
    }
}

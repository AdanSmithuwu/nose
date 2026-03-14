package com.comercialvalerio.application.mapper.report;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comercialvalerio.application.dto.report.ResumenDiaDto;
import com.comercialvalerio.application.dto.report.ResumenDiarioDto;
import com.comercialvalerio.application.dto.report.ResumenMensualDto;
import com.comercialvalerio.application.dto.report.ResumenCategoriaDto;
import com.comercialvalerio.application.dto.report.RotacionDto;
import com.comercialvalerio.application.dto.report.PagoMetodoDiaDto;
import java.util.List;
import com.comercialvalerio.domain.view.report.ResumenModalidad;
import com.comercialvalerio.domain.view.report.RotacionProducto;
import com.comercialvalerio.domain.view.report.TransaccionesDia;
import com.comercialvalerio.domain.view.report.PagoMetodoDia;
import com.comercialvalerio.domain.view.report.ResumenCategoria;

@Mapper(componentModel = "cdi")
public interface ReporteViewDtoMapper {
    PagoMetodoDiaDto toDto(PagoMetodoDia e);

    default ResumenDiarioDto toDto(TransaccionesDia d, List<PagoMetodoDia> pagos) {
        List<PagoMetodoDiaDto> list = pagos == null ? java.util.List.of() : pagos.stream().map(this::toDto).toList();
        if (d == null)
            return new ResumenDiarioDto(null,0,java.math.BigDecimal.ZERO,0,0,java.math.BigDecimal.ZERO,java.math.BigDecimal.ZERO,list);
        return new ResumenDiarioDto(d.dia(), d.numTransacciones(), d.ingresosDia(),
                d.numVentas(), d.numPedidos(), d.montoBruto(), d.montoNeto(), list);
    }

    @Mapping(target = "fecha", source = "dia")
    @Mapping(target = "numTransacciones", expression = "java(d.numTransacciones())")
    @Mapping(target = "monto", source = "ingresosDia")
    ResumenDiaDto toDiaDto(TransaccionesDia d);

    @Mapping(target = "unidades", source = "totalUnidadesVendidas")
    @Mapping(target = "categoria", source = "categoria")
    @Mapping(target = "importe", source = "importeTotal")
    RotacionDto toDto(RotacionProducto r);

    ResumenCategoriaDto toDto(ResumenCategoria c);

    default ResumenMensualDto toDto(ResumenModalidad resumen,
                                     java.util.List<ResumenDiaDto> dias,
                                     java.util.List<ResumenCategoriaDto> categorias) {
        if (resumen == null) {
            return new ResumenMensualDto(dias, categorias, 0, java.math.BigDecimal.ZERO,
                    0, java.math.BigDecimal.ZERO, 0, java.math.BigDecimal.ZERO);
        }
        return new ResumenMensualDto(dias, categorias,
                resumen.numTransMinorista(), resumen.montoMinorista(),
                resumen.numTransEspecial(), resumen.montoEspecial(),
                resumen.numPedidosDomicilio(), resumen.montoPedidosDomicilio());
    }
}

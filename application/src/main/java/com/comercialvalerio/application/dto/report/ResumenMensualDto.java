package com.comercialvalerio.application.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record ResumenMensualDto(List<ResumenDiaDto> dias,
                                List<ResumenCategoriaDto> categorias,
                                long numTransMinorista,
                                BigDecimal montoMinorista,
                                long numTransEspecial,
                                BigDecimal montoEspecial,
                                long numPedidosDomicilio,
                                BigDecimal montoPedidosDomicilio) {}

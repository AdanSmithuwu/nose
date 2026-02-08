package com.comercialvalerio.infrastructure.service.report;

import java.math.BigDecimal;

public record ResumenModalidadRow(Long numTransMinorista, BigDecimal montoMinorista,
                                   Long numTransEspecial, BigDecimal montoEspecial,
                                   Long numPedidosDomicilio, BigDecimal montoPedidosDomicilio) {}

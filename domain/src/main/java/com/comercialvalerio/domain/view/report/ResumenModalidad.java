package com.comercialvalerio.domain.view.report;

import java.math.BigDecimal;

/** Resumen mensual por modalidad de pago. */
public record ResumenModalidad(int anio,
                               int mes,
                               long numTransMinorista,
                               BigDecimal montoMinorista,
                               long numTransEspecial,
                               BigDecimal montoEspecial,
                               long numPedidosDomicilio,
                               BigDecimal montoPedidosDomicilio) {}

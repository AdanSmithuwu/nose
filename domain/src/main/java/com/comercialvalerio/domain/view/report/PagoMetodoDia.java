package com.comercialvalerio.domain.view.report;

import java.math.BigDecimal;

/** Monto recaudado por método de pago en un día. */
public record PagoMetodoDia(String metodo, BigDecimal monto) {}

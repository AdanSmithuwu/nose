package com.comercialvalerio.application.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResumenDiarioDto(LocalDate fecha,
                               long numTransacciones,
                               BigDecimal montoTotal,
                               long numVentas,
                               long numPedidos,
                               BigDecimal montoBruto,
                               BigDecimal montoNeto,
                               List<PagoMetodoDiaDto> pagos) {}

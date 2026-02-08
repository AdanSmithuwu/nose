package com.comercialvalerio.infrastructure.service.report;

import java.util.List;

public record MensualData(List<TransaccionDiaRow> dias,
                          List<ResumenCategoriaRow> categorias,
                          ResumenModalidadRow resumen) {}

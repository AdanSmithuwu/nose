package com.comercialvalerio.infrastructure.service.report;

import java.util.List;

public record DiarioData(ResumenDiaRow resumen, List<PagoMetodoRow> pagos) {}

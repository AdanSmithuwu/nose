package com.comercialvalerio.domain.notification;

import java.time.LocalDate;

/** Evento lanzado cuando falla la generación del reporte diario. */
public record ReporteDiarioFallidoEvent(LocalDate fecha, Throwable error) {}

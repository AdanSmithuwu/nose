package com.comercialvalerio.domain.service;

import java.time.LocalDate;

/**
 * Servicio de dominio encargado de construir los PDFs de reportes.
 */
public interface ReporteGenerator {

    /**
     * Genera el reporte diario para la fecha dada.
     *
     * @param fecha día a consultar
     * @return bytes del PDF generado
     */
    byte[] generarReporteDiario(LocalDate fecha);

    /**
     * Genera el reporte mensual indicado.
     *
     * @param anio  año del reporte
     * @param mes   mes del reporte (1-12)
     * @param incluirResumen si debe incluir el resumen por modalidad
     * @return bytes del PDF generado
     */
    byte[] generarReporteMensual(int anio, int mes, boolean incluirResumen);

    /**
     * Genera el reporte de rotación para el periodo indicado.
     *
     * @param desde fecha inicial del rango
     * @param hasta fecha final del rango
     * @param top   límite de productos en el reporte
     * @return bytes del PDF generado
     */
    byte[] generarReporteRotacion(LocalDate desde, LocalDate hasta, Integer top);
}

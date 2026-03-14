package com.comercialvalerio.application.service;
import java.time.LocalDate;
import java.util.List;

import com.comercialvalerio.application.dto.ReporteCreateDto;
import com.comercialvalerio.application.dto.ReporteDto;
import com.comercialvalerio.application.dto.ReportePdfDto;

public interface ReporteService {
    List<ReporteDto> listar();
    List<ReporteDto> listarPorEmpleado(Integer idEmpleado);
    ReporteDto       crear(ReporteCreateDto dto);
    ReportePdfDto generarDiario(LocalDate fecha);
    /** Genera y almacena el reporte diario devolviendo su PDF. */
    ReportePdfDto guardarDiario(LocalDate fecha);
    ReportePdfDto generarMensual(int anio, int mes, boolean incluirResumen);
    /** Genera y almacena el reporte mensual devolviendo su PDF. */
    ReportePdfDto guardarMensual(int anio, int mes, boolean incluirResumen);
    /** Devuelve el reporte diario existente para la fecha indicada. */
    ReporteDto buscarDiario(LocalDate fecha);
    /** Devuelve el reporte mensual existente para el periodo indicado. */
    ReporteDto buscarMensual(int anio, int mes);
    ReportePdfDto generarRotacion(java.time.LocalDate desde, java.time.LocalDate hasta, Integer top);
    /** Genera y almacena el reporte de rotación devolviendo su PDF. */
    ReportePdfDto guardarRotacion(java.time.LocalDate desde, java.time.LocalDate hasta, Integer top);
    byte[] descargarPdf(Integer idReporte);

    /* Datos para vista previa */
    com.comercialvalerio.application.dto.report.ResumenDiarioDto datosDiario(java.time.LocalDate fecha);
    com.comercialvalerio.application.dto.report.ResumenMensualDto datosMensual(int anio, int mes);
    java.util.List<com.comercialvalerio.application.dto.report.RotacionDto> datosRotacion(java.time.LocalDate desde,
                                                                                           java.time.LocalDate hasta,
                                                                                           Integer top);
}

package com.comercialvalerio.domain.repository.report;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio de solo lectura para las distintas vistas de reportes.
 * Los tipos retornados son parametrizables para mantener este módulo
 * independiente de la capa de infraestructura.
 */
public interface ReporteViewRepository<D, P, R, T, C> {

    /** Contenedor para el resumen diario y totales de pagos. */
    record DiarioData<D, P>(D resumen, List<P> pagos) {}

    /** Datos de un día específico. */
    DiarioData<D, P> diario(LocalDate fecha);

    /** Datos para un rango de fechas. */
    List<D> rango(LocalDate desde, LocalDate hasta);

    /** Resumen mensual por modalidad de pago. */
    R resumenMensual(int anio, int mes);

    /** Rotación de productos en un rango de fechas. */
    List<T> rotacion(LocalDate desde, LocalDate hasta, Integer top);

    /** Resumen mensual por categoría de productos. */
    List<C> resumenMensualCategoria(int anio, int mes);
}

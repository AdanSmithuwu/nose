package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.Reporte;
import java.util.List;

/* Historial de reportes generados por el sistema */
public interface ReporteRepository {
    List<Reporte> findAll();
    List<Reporte> findByEmpleado(Integer idEmpleado);
    java.util.Optional<Reporte> findById(Integer idReporte);
    /** Busca reporte mensual por año y mes, si existe. */
    java.util.Optional<Reporte> findMensual(int anio, int mes);
    /** Busca reporte diario por fecha, si existe. */
    java.util.Optional<Reporte> findDiario(java.time.LocalDate fecha);
    /* Persiste el historial de generación (INSERT en dbo.Reporte). */
    void save(Reporte reporte);
}

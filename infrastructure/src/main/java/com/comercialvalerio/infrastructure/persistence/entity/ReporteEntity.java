package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedStoredProcedureQueries;
import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.StoredProcedureParameter;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.Table;

@Entity(name = "Reporte")
@Table(name = "Reporte")
@NamedQueries({
    @NamedQuery(name = "Reporte.findAll",
                query = "SELECT r FROM Reporte r ORDER BY r.fechaGeneracion DESC"),
    @NamedQuery(name = "Reporte.findByEmpleado",
                query = "SELECT r FROM Reporte r "
                      + "WHERE r.empleado.idPersona = :empId "
                      + "ORDER BY r.fechaGeneracion DESC"),
    @NamedQuery(name = "Reporte.findMensual",
                query = "SELECT r FROM Reporte r "
                      + "WHERE r.tipoReporte = 'Mensual' "
                      + "AND r.desde = :desde "
                      + "AND r.hasta = :hasta"),
    @NamedQuery(name = "Reporte.findDiario",
                query = "SELECT r FROM Reporte r "
                      + "WHERE r.tipoReporte = 'Diario' "
                      + "AND r.desde = :fecha "
                      + "AND r.hasta = :fecha")
})
    @NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(name = "Reporte.generarDiario",
            procedureName = "dbo.sp_GenerarReporteDiario",
            parameters = {
                @StoredProcedureParameter(name = "fecha", mode = ParameterMode.IN,
                                      type = java.sql.Date.class)
        }),
        @NamedStoredProcedureQuery(name = "Reporte.generarMensual",
            procedureName = "dbo.sp_GenerarReporteMensual",
            parameters = {
                @StoredProcedureParameter(name = "anio",        mode = ParameterMode.IN,
                                          type = Integer.class),
                @StoredProcedureParameter(name = "mes",         mode = ParameterMode.IN,
                                          type = Integer.class),
                @StoredProcedureParameter(name = "conResumen",  mode = ParameterMode.IN,
                                          type = Boolean.class)
            }),
        @NamedStoredProcedureQuery(name = "Reporte.generarRotacion",
            procedureName = "dbo.sp_GenerarReporteRotacion",
            parameters = {
                @StoredProcedureParameter(name = "desde", mode = ParameterMode.IN,
                                      type = java.time.LocalDateTime.class),
            @StoredProcedureParameter(name = "hasta", mode = ParameterMode.IN,
                                      type = java.time.LocalDateTime.class),
            @StoredProcedureParameter(name = "top",   mode = ParameterMode.IN,
                                      type = Integer.class)
        })
})
public class ReporteEntity implements Serializable {
    /* ---------- Clave primaria ---------- */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idReporte")
    private Integer idReporte;
    /* ---------- Datos básicos ---------- */
    @Column(name = "tipoReporte", nullable = false, length = LEN_TIPO_REPORTE)
    private String tipoReporte;
    @Column(name = "desde", nullable = false)
    private LocalDate desde;
    @Column(name = "hasta", nullable = false)
    private LocalDate hasta;
    @Column(name = "filtros", length = LEN_FILTROS_REPORTE)
    private String  filtros;
    @Column(name="fechaGeneracion", nullable=false)
    private LocalDateTime fechaGeneracion;
    /* ---------- Contenido PDF ---------- */
    @Lob @Column(name = "bytesPdf", nullable = false)
    private byte[]  bytesPdf;
    /* ---------- Relación ---------- */
    @ManyToOne(optional = false) @JoinColumn(name = "idEmpleado", nullable = false)
    private EmpleadoEntity empleado;
    public ReporteEntity() {
    }
    public ReporteEntity(Integer idReporte) {
        this.idReporte = idReporte;
    }
    public ReporteEntity(Integer idReporte, String tipoReporte, LocalDate desde, LocalDate hasta, byte[] bytesPdf, LocalDateTime fechaGeneracion) {
        this.idReporte = idReporte;
        this.tipoReporte = tipoReporte;
        this.desde = desde;
        this.hasta = hasta;
        this.bytesPdf = bytesPdf;
        this.fechaGeneracion = fechaGeneracion;
    }
    public Integer getIdReporte() {
        return idReporte;
    }
    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
    }
    public String getTipoReporte() {
        return tipoReporte;
    }
    public void setTipoReporte(String tipoReporte) {
        this.tipoReporte = tipoReporte;
    }
    public LocalDate getDesde() {
        return desde;
    }
    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }
    public LocalDate getHasta() {
        return hasta;
    }
    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
    }
    public String getFiltros() {
        return filtros;
    }
    public void setFiltros(String filtros) {
        this.filtros = filtros;
    }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
      }
    public byte[] getBytesPdf() {
        return bytesPdf;
    }
    public void setBytesPdf(byte[] bytesPdf) {
        this.bytesPdf = bytesPdf;
    }
    public EmpleadoEntity getEmpleado() {
        return empleado;
    }
    public void setEmpleado(EmpleadoEntity empleado) {
        this.empleado = empleado;
    }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.Reporte[ idReporte=" + idReporte + " ]";
    }
}

package com.comercialvalerio.domain.model;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationUtils.*;
import com.comercialvalerio.common.DbConstraints;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * Historial de reportes PDF generados por el sistema.
 * Los tipos registrados son DIARIO, MENSUAL y ROTACION.
 *
 * <p>Del DDL:</p>
 * <ul>
 *   <li>PK <code>idReporte</code></li>
 *   <li><code>tipoReporte</code> NVARCHAR(20)
 *       (<code>DbConstraints.LEN_TIPO_REPORTE</code>) con valores de catálogo
 *       (“Diario”, “Mensual”, “Rotacion”).</li>
 *   <li>FK a <b>empleado</b> (usuario que lo generó)</li>
 * </ul>
 */
public class Reporte extends BaseEntity<Integer> {

    private Integer       idReporte;        // PK autogenerada
    private TipoReporte   tipoReporte;      // obligatorio, catálogo
    private Empleado      empleado;         // obligatorio
    private LocalDate     desde;            // rango de fechas
    private LocalDate     hasta;
    private String        filtros;          // JSON/String opcional, ≤ DbConstraints.LEN_FILTROS_REPORTE
    private byte[]        bytesPdf;         // no nulo
    private LocalDateTime fechaGeneracion;  // no futura

    /* ---------- Constructor con invariantes ---------- */
    public Reporte(Integer idReporte, TipoReporte tipoReporte, Empleado empleado,
                   LocalDate desde, LocalDate hasta, String filtros,
                   byte[] bytesPdf, LocalDateTime fechaGeneracion) {

        validarTipo(tipoReporte);
        validarEmpleado(empleado);
        validarRangoFechas(desde, hasta);
        validarFiltros(filtros);
        validarPdf(bytesPdf);
        validarFechaGeneracion(fechaGeneracion);

        this.idReporte        = idReporte;
        this.tipoReporte      = tipoReporte;
        this.empleado         = empleado;
        this.desde            = desde;
        this.hasta            = hasta;
        this.filtros          = filtros == null ? null : filtros.trim();
        this.bytesPdf         = bytesPdf.clone();
        this.fechaGeneracion  = fechaGeneracion;
    }

    public Reporte() {}

    /* ---------- Getters ---------- */
    public Integer       getIdReporte()        { return idReporte; }
    @Override
    public Integer getId() { return idReporte; }
    public TipoReporte   getTipoReporte()      { return tipoReporte; }
    public Empleado      getEmpleado()         { return empleado; }
    public LocalDate     getDesde()            { return desde; }
    public LocalDate     getHasta()            { return hasta; }
    public String        getFiltros()          { return filtros; }
    public byte[]        getBytesPdf()         { return bytesPdf.clone(); }
    public LocalDateTime getFechaGeneracion()  { return fechaGeneracion; }

    /* ---------- Setters con validaciones ---------- */

    public void setIdReporte(Integer id) {
        requireIdNotSet(this.idReporte, id,
                "El idReporte ya fue asignado y no puede modificarse");
        this.idReporte = id;
    }

    public void setTipoReporte(TipoReporte tipo) {
        validarTipo(tipo);
        this.tipoReporte = tipo;
    }

    public void setEmpleado(Empleado emp) {
        validarEmpleado(emp);
        this.empleado = emp;
    }

    public void setDesde(LocalDate desde) {
        validarRangoFechas(desde, this.hasta);
        this.desde = desde;
    }

    public void setHasta(LocalDate hasta) {
        validarRangoFechas(this.desde, hasta);
        this.hasta = hasta;
    }

    public void setFiltros(String filtros) {
        validarFiltros(filtros);
        this.filtros = filtros == null ? null : filtros.trim();
    }

    public void setBytesPdf(byte[] pdf) {
        validarPdf(pdf);
        this.bytesPdf = pdf.clone();
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        validarFechaGeneracion(fechaGeneracion);
        this.fechaGeneracion = fechaGeneracion;
    }

    /* ---------- Validaciones internas ---------- */

    private void validarTipo(TipoReporte t) {
        if (t == null)
            throw new BusinessRuleViolationException(
                "El tipo de reporte es obligatorio");
    }

    private void validarEmpleado(Empleado e) {
        if (e == null)
            throw new BusinessRuleViolationException(
                "Debe registrarse el empleado que generó el reporte");
    }

    private void validarRangoFechas(LocalDate d, LocalDate h) {
        if (d != null && h != null && d.isAfter(h))
            throw new BusinessRuleViolationException(
                "La fecha 'desde' no puede ser posterior a 'hasta'");
    }

    private void validarFiltros(String f) {
        if (f != null && f.length() > DbConstraints.LEN_FILTROS_REPORTE)
            throw new BusinessRuleViolationException(
                "La cadena de filtros supera " + DbConstraints.LEN_FILTROS_REPORTE + " caracteres");
    }

    private void validarPdf(byte[] pdf) {
        if (pdf == null || pdf.length == 0)
            throw new BusinessRuleViolationException(
                "El PDF del reporte no puede estar vacío");
    }

    private void validarFechaGeneracion(LocalDateTime f) {
        if (f == null || f.isAfter(LocalDateTime.now()))
            throw new BusinessRuleViolationException(
                "La fecha de generación no puede ser futura");
    }
}

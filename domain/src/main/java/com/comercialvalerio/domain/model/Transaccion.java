package com.comercialvalerio.domain.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationMessages.PAYMENTS_TOTAL_MISMATCH;
import static com.comercialvalerio.domain.util.ValidationMessages.STATE_REQUIRED;
import static com.comercialvalerio.domain.util.ValidationUtils.requireIdNotSet;
import static com.comercialvalerio.domain.util.ValidationUtils.requirePrecision;

/*
 * Transacción comercial (abstracta): venta, pedido, devolución, etc.
 *
 * <p>Del DDL:</p>
 * <ul>
 *   <li>PK <code>idTransaccion</code></li>
 *   <li>FK a <b>estado</b> (módulo = Transaccion)</li>
 *   <li>FK a <b>empleado</b> (responsable) y obligatorio <b>cliente</b></li>
 * </ul>
 */
public class Transaccion extends BaseEntity<Integer> {

    /* ---------- Atributos ---------- */
    private Integer       idTransaccion;
    private LocalDateTime fecha;              // no futura
    private Estado        estado;             // obligatorio
    private BigDecimal    totalBruto;         // ≥ 0
    private BigDecimal    descuento;          // ≥ 0 y ≤ totalBruto
    private BigDecimal    cargo;              // ≥ 0
    private BigDecimal    totalNeto;          // totalBruto - descuento + cargo
    private String        observacion;        // opcional, ≤ 120
    private String        motivoCancelacion;  // requerido si estado=“Cancelada”
    private Empleado      empleado;           // obligatorio
    private Cliente       cliente;            // obligatorio
    private List<DetalleTransaccion> detalles = new ArrayList<>();
    private List<PagoTransaccion>    pagos    = new ArrayList<>();

    /* ---------- Constructor con invariantes ---------- */
    public Transaccion(Integer idTransaccion, LocalDateTime fecha, Estado estado,
                       BigDecimal totalBruto, BigDecimal descuento,
                       BigDecimal cargo, BigDecimal totalNeto,
                       String observacion, String motivoCancelacion,
                       Empleado empleado, Cliente cliente) {

        validarFecha(fecha);
        validarEstado(estado);
        validarTotales(totalBruto, descuento, cargo, totalNeto);
        validarObservacion(observacion);
        validarMotivoCancelacion(motivoCancelacion, estado);
        validarEmpleado(empleado);
        validarCliente(cliente);

        this.idTransaccion     = idTransaccion;
        this.fecha             = fecha;
        this.estado            = estado;
        this.totalBruto        = totalBruto;
        this.descuento         = descuento;
        this.cargo             = cargo;
        this.totalNeto         = totalNeto;
        this.observacion       = observacion == null ? null : observacion.trim();
        this.motivoCancelacion = motivoCancelacion == null ? null : motivoCancelacion.trim();
        this.empleado          = empleado;
        this.cliente           = cliente;
    }

    public Transaccion() {}

    /* ---------- Getters ---------- */
    public Integer       getIdTransaccion()     { return idTransaccion; }
    @Override
    public Integer getId() { return idTransaccion; }
    public LocalDateTime getFecha()             { return fecha; }
    public Estado        getEstado()            { return estado; }
    public BigDecimal    getTotalBruto()        { return totalBruto; }
    public BigDecimal    getDescuento()         { return descuento; }
    public BigDecimal    getCargo()             { return cargo; }
    public BigDecimal    getTotalNeto()         { return totalNeto; }
    public String        getObservacion()       { return observacion; }
    public String        getMotivoCancelacion() { return motivoCancelacion; }
    public Empleado      getEmpleado()          { return empleado; }
    public Cliente       getCliente()           { return cliente; }

    /* ---------- Setters con validaciones ---------- */

    public void setIdTransaccion(Integer id) {
        requireIdNotSet(this.idTransaccion, id,
                "El idTransaccion ya fue asignado y no puede modificarse");
        this.idTransaccion = id;
    }

    public void setFecha(LocalDateTime fecha) {
        validarFecha(fecha);
        this.fecha = fecha;
    }

    public void setEstado(Estado estado) {
        validarEstado(estado);
        boolean pendienteMotivo =
                EstadoNombre.CANCELADA.getNombre().equalsIgnoreCase(estado.getNombre())
                && this.motivoCancelacion == null;
        if (!pendienteMotivo)
            validarMotivoCancelacion(this.motivoCancelacion, estado);
        this.estado = estado;
    }

    public void setTotales(BigDecimal totalBruto, BigDecimal descuento,
                           BigDecimal cargo, BigDecimal totalNeto) {
        validarTotales(totalBruto, descuento, cargo, totalNeto);
        this.totalBruto = totalBruto;
        this.descuento  = descuento;
        this.cargo      = cargo;
        this.totalNeto  = totalNeto;
    }

    public void setObservacion(String observacion) {
        validarObservacion(observacion);
        this.observacion = observacion == null ? null : observacion.trim();
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        validarMotivoCancelacion(motivoCancelacion, this.estado);
        this.motivoCancelacion = motivoCancelacion == null ? null : motivoCancelacion.trim();
    }

    public void setEmpleado(Empleado empleado) {
        validarEmpleado(empleado);
        this.empleado = empleado;
    }

    public void setCliente(Cliente cliente) {
        validarCliente(cliente);
        this.cliente = cliente;
    }

    /* Obtiene la lista de detalles (líneas) de esta transacción. */
    public List<DetalleTransaccion> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    /* Asigna la lista completa de detalles (sin recalcular totales). */
    public void setDetalles(List<DetalleTransaccion> det) {
        detalles = det == null ? new ArrayList<>() : new ArrayList<>(det);
    }

    /* Agrega una línea al detalle de la transacción. */
    public void addDetalle(DetalleTransaccion d) {
        Objects.requireNonNull(d);
        for (DetalleTransaccion det : detalles) {
            Integer id1 = det.getProducto() == null ? null
                    : det.getProducto().getIdProducto();
            Integer id2 = d.getProducto() == null ? null
                    : d.getProducto().getIdProducto();
            if (id1 == null || id2 == null) continue;

            Integer ts1 = det.getTallaStock() == null ? null
                    : det.getTallaStock().getIdTallaStock();
            Integer ts2 = d.getTallaStock() == null ? null
                    : d.getTallaStock().getIdTallaStock();

            if (Objects.equals(id1, id2) && Objects.equals(ts1, ts2)) {
                throw new BusinessRuleViolationException(
                        "Ya existe un detalle con el mismo producto y talla");
            }
        }
        detalles.add(d);
    }

    /**
     * Variante que además recalcula los totales de la transacción.
     * Úsese al crear nuevas transacciones para mantener las invariantes.
     */
    public void agregarDetalle(DetalleTransaccion d) {
        addDetalle(d);
        if (d.getSubtotal() != null) {
            if (this.totalBruto == null) this.totalBruto = BigDecimal.ZERO;
            this.totalBruto = this.totalBruto.add(d.getSubtotal());
        }
        recalcularTotales();
    }

    /* Obtiene los pagos asociados a esta transacción. */
    public List<PagoTransaccion> getPagos() {
        return Collections.unmodifiableList(pagos);
    }

    /* Asigna la lista completa de pagos (sin recalcular totales). */
    public void setPagos(List<PagoTransaccion> p) {
        pagos = p == null ? new ArrayList<>() : new ArrayList<>(p);
    }

    /* Agrega un pago a esta transacción. */
    public void addPago(PagoTransaccion p) {
        Objects.requireNonNull(p);
        pagos.add(p);
    }

    /**
     * Variante que recalcula totales tras registrar el pago.
     */
    public void agregarPago(PagoTransaccion p) {
        addPago(p);
        recalcularTotales();
    }

    /** Devuelve el monto total pagado en esta transacción. */
    public BigDecimal getTotalPagado() {
        return pagos.stream()
                .map(PagoTransaccion::getMonto)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Valida que los pagos sumen exactamente el total neto. */
    public void validarTotalPagado() {
        if (totalNeto == null) return;
        BigDecimal pagado = getTotalPagado();
        if (pagado.compareTo(totalNeto) != 0)
            throw new BusinessRuleViolationException(PAYMENTS_TOTAL_MISMATCH);
    }

    private void recalcularTotales() {
        if (this.totalBruto == null) this.totalBruto = BigDecimal.ZERO;
        if (this.descuento == null)  this.descuento  = BigDecimal.ZERO;
        if (this.cargo == null)      this.cargo      = BigDecimal.ZERO;

        this.totalNeto = this.totalBruto.subtract(this.descuento)
                                       .add(this.cargo);
    }
    /* ---------- Validaciones internas ---------- */

    private void validarFecha(LocalDateTime f) {
        if (f == null || f.isAfter(LocalDateTime.now()))
            throw new BusinessRuleViolationException(
                "La fecha de la transacción no puede ser futura");
    }

    private void validarEstado(Estado e) {
        if (e == null)
            throw new BusinessRuleViolationException(STATE_REQUIRED);
    }

    private void validarTotales(BigDecimal bruto, BigDecimal desc,
                                BigDecimal cargo, BigDecimal neto) {
        if (bruto == null || bruto.compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessRuleViolationException("totalBruto debe ser ≥ 0");
        requirePrecision(bruto, DbConstraints.PRECIO_PRECISION, DbConstraints.PRECIO_SCALE,
                "totalBruto fuera de rango (" + DbConstraints.PRECIO_PRECISION + ','
                        + DbConstraints.PRECIO_SCALE + ')');
        if (desc == null || desc.compareTo(BigDecimal.ZERO) < 0
            || desc.compareTo(bruto) > 0)
            throw new BusinessRuleViolationException("descuento inválido");
        requirePrecision(desc, DbConstraints.PRECIO_PRECISION, DbConstraints.PRECIO_SCALE,
                "descuento fuera de rango (" + DbConstraints.PRECIO_PRECISION + ','
                        + DbConstraints.PRECIO_SCALE + ')');
        if (cargo == null || cargo.compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessRuleViolationException("cargo debe ser ≥ 0");
        requirePrecision(cargo, DbConstraints.PRECIO_PRECISION, DbConstraints.PRECIO_SCALE,
                "cargo fuera de rango (" + DbConstraints.PRECIO_PRECISION + ',' + DbConstraints.PRECIO_SCALE + ")");
        BigDecimal esperado = bruto.subtract(desc).add(cargo);
        if (neto == null || esperado.compareTo(neto) != 0)
            throw new BusinessRuleViolationException(
                "totalNeto debe ser totalBruto − descuento + cargo");
        requirePrecision(neto, DbConstraints.PRECIO_PRECISION, DbConstraints.PRECIO_SCALE,
                "totalNeto fuera de rango (" + DbConstraints.PRECIO_PRECISION + ',' + DbConstraints.PRECIO_SCALE + ")");
    }

    private void validarObservacion(String o) {
        if (o != null && o.length() > DbConstraints.LEN_OBSERVACION)
            throw new BusinessRuleViolationException(
                "La observación supera " + DbConstraints.LEN_OBSERVACION + " caracteres");
    }

    private void validarMotivoCancelacion(String motivo, Estado e) {
        boolean esCancelado = e != null
                && EstadoNombre.CANCELADA.getNombre().equalsIgnoreCase(e.getNombre());
        if (esCancelado && (motivo == null || motivo.isBlank()))
            throw new BusinessRuleViolationException(
                "Debe indicar motivoCancelacion cuando la transacción está CANCELADA");
        if (motivo != null && motivo.length() > DbConstraints.LEN_OBSERVACION)
            throw new BusinessRuleViolationException(
                "El motivo de cancelación supera " + DbConstraints.LEN_OBSERVACION + " caracteres");
    }

    private void validarEmpleado(Empleado emp) {
        if (emp == null)
            throw new BusinessRuleViolationException(
                "Debe registrarse el empleado responsable");
    }

    private void validarCliente(Cliente cli) {
        if (cli == null)
            throw new BusinessRuleViolationException(
                "Debe asignarse un cliente a la transacción");
    }
}

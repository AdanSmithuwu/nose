package com.comercialvalerio.domain.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationMessages.QUANTITY_GREATER_THAN_ZERO;
import static com.comercialvalerio.domain.util.ValidationUtils.requireIdNotSet;
import static com.comercialvalerio.domain.util.ValidationUtils.requireNotNull;
import static com.comercialvalerio.domain.util.ValidationUtils.requirePositive;
import static com.comercialvalerio.domain.util.ValidationUtils.requirePrecision;

/*
 * Registro de un movimiento de inventario (Kardex).
 *
 * <p>Del DDL:</p>
 * <ul>
 *   <li>PK <code>idMovimiento</code></li>
 *   <li>FK a <b>producto</b>, <b>talla_stock</b> (opcional), <b>tipo_movimiento</b>, <b>empleado</b></li>
 * </ul>
 */
public class MovimientoInventario extends BaseEntity<Integer> {

    private Integer       idMovimiento;   // PK autogenerada
    private Producto      producto;       // obligatorio
    private TallaStock    tallaStock;     // puede ser null
    private TipoMovimiento tipoMovimiento;// obligatorio
    private BigDecimal    cantidad;       // > 0 (valor absoluto)
    private String        motivo;         // opcional, máx. 80
    private LocalDateTime fechaHora;      // no futura
    private Empleado      empleado;       // obligatorio

    /**
     * Crea un movimiento con la cantidad indicada (positiva).
     * El signo que afecta al stock se determina únicamente por el
     * {@link TipoMovimiento} del movimiento.
     */
    public static MovimientoInventario crear(Producto producto,
                                             TallaStock tallaStock,
                                             TipoMovimiento tipoMovimiento,
                                             BigDecimal cantidad,
                                             String motivo,
                                             Empleado empleado) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setProducto(producto);
        mov.setTallaStock(tallaStock);
        mov.setMotivo(motivo);
        mov.setTipoMovimiento(tipoMovimiento);

        mov.setCantidad(cantidad);
        mov.setFechaHora(LocalDateTime.now());
        mov.setEmpleado(empleado);
        return mov;
    }

    /* ---------- Constructor con invariantes ---------- */
    public MovimientoInventario(Integer idMovimiento, Producto producto,
                                TallaStock tallaStock, TipoMovimiento tipoMovimiento,
                                BigDecimal cantidad, String motivo,
                                LocalDateTime fechaHora, Empleado empleado) {

        validarProducto(producto);
        validarTallaStock(tallaStock, producto);
        validarTipoMovimiento(tipoMovimiento);
        validarCantidad(cantidad);
        validarMotivo(motivo);
        validarMotivoAjuste(motivo, tipoMovimiento);
        validarFechaHora(fechaHora);
        validarEmpleado(empleado);

        this.idMovimiento   = idMovimiento;
        this.producto       = producto;
        this.tallaStock     = tallaStock;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad       = cantidad;
        this.motivo         = motivo == null ? null : motivo.trim();
        this.fechaHora      = fechaHora;
        this.empleado       = empleado;
    }

    public MovimientoInventario() {}

    /* ---------- Getters ---------- */
    public Integer       getIdMovimiento()   { return idMovimiento; }
    @Override
    public Integer getId() { return idMovimiento; }
    public Producto      getProducto()       { return producto; }
    public TallaStock    getTallaStock()     { return tallaStock; }
    public TipoMovimiento getTipoMovimiento(){ return tipoMovimiento; }
    public BigDecimal    getCantidad()       { return cantidad; }
    public String        getMotivo()         { return motivo; }
    public LocalDateTime getFechaHora()      { return fechaHora; }
    public Empleado      getEmpleado()       { return empleado; }

    /* ---------- Setters con validaciones ---------- */

    public void setIdMovimiento(Integer id) {
        requireIdNotSet(this.idMovimiento, id,
                "El idMovimiento ya fue asignado y no puede modificarse");
        this.idMovimiento = id;
    }

    public void setProducto(Producto producto) {
        validarProducto(producto);
        this.producto = producto;
    }

    public void setTallaStock(TallaStock tallaStock) {
        validarTallaStock(tallaStock, this.producto);
        this.tallaStock = tallaStock;
    }

    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) {
        validarTipoMovimiento(tipoMovimiento);
        this.tipoMovimiento = tipoMovimiento;
        if (this.motivo != null)
            validarMotivoAjuste(this.motivo, tipoMovimiento);
    }

    public void setCantidad(BigDecimal cantidad) {
        validarCantidad(cantidad);
        this.cantidad = cantidad;
    }

    public void setMotivo(String motivo) {
        validarMotivo(motivo);
        this.motivo = motivo == null ? null : motivo.trim();
        if (this.tipoMovimiento != null)
            validarMotivoAjuste(this.motivo, this.tipoMovimiento);
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        validarFechaHora(fechaHora);
        this.fechaHora = fechaHora;
    }

    public void setEmpleado(Empleado empleado) {
        validarEmpleado(empleado);
        this.empleado = empleado;
    }

    /* ---------- Validaciones internas ---------- */

    private void validarProducto(Producto p) {
        requireNotNull(p, "El producto es obligatorio en un movimiento de inventario");
    }

    private void validarTallaStock(TallaStock ts, Producto p) {
        if (ts != null && p != null && !Objects.equals(ts.getProducto().getIdProducto(),
                                                       p.getIdProducto()))
            throw new BusinessRuleViolationException(
                "La talla indicada no corresponde al producto del movimiento");
    }

    private void validarTipoMovimiento(TipoMovimiento tm) {
        requireNotNull(tm, "El tipo de movimiento es obligatorio");
    }

    private void validarCantidad(BigDecimal c) {
        requirePositive(c, QUANTITY_GREATER_THAN_ZERO);
        requirePrecision(c, DbConstraints.STOCK_PRECISION, DbConstraints.STOCK_SCALE,
                "cantidad fuera de rango (" + DbConstraints.STOCK_PRECISION + ',' + DbConstraints.STOCK_SCALE + ")");
    }

    private void validarMotivo(String m) {
        if (m != null && m.length() > DbConstraints.LEN_MOTIVO)
            throw new BusinessRuleViolationException(
                "El motivo no puede superar " + DbConstraints.LEN_MOTIVO + " caracteres");
    }

    private void validarFechaHora(LocalDateTime f) {
        if (f == null || f.isAfter(LocalDateTime.now()))
            throw new BusinessRuleViolationException(
                "La fecha del movimiento no puede ser futura");
    }

    private void validarEmpleado(Empleado e) {
        requireNotNull(e, "Debe registrarse el empleado responsable del movimiento");
    }

    /* Motivo obligatorio cuando el movimiento es de tipo Ajuste */
    private void validarMotivoAjuste(String motivo, TipoMovimiento tm) {
        if (tm != null
                && TipoMovimientoNombre.fromNombre(tm.getNombre()) == TipoMovimientoNombre.AJUSTE
                && (motivo == null || motivo.isBlank()))
            throw new BusinessRuleViolationException(
                "El motivo es obligatorio para movimientos de ajuste");
    }
}

package com.comercialvalerio.domain.model;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.comercialvalerio.common.DbConstraints;
import static com.comercialvalerio.domain.util.ValidationUtils.requireIdNotSet;
import static com.comercialvalerio.domain.util.ValidationUtils.requireNotNull;
import static com.comercialvalerio.domain.util.ValidationUtils.requirePositive;
import static com.comercialvalerio.domain.util.ValidationUtils.requirePrecision;

/*
 * Pago aplicado a una transacción.
 *
 * <p>Del DDL:</p>
 * <ul>
 *   <li>PK <code>idPago</code></li>
 *   <li>FK a <b>transaccion</b> y <b>metodo_pago</b></li>
 *   <li>Restricción <code>UNIQUE(idTransaccion,idMetodoPago)</code> para
 *       evitar pagos duplicados con el mismo método.</li>
 * </ul>
 */
public class PagoTransaccion extends BaseEntity<Integer> {

    private Integer     idPago;        // PK autogenerada
    private Transaccion transaccion;   // obligatorio
    private MetodoPago  metodoPago;    // obligatorio
    private BigDecimal  monto;         // > 0 (2 decimales)

    /* ---------- Constructor con invariantes ---------- */
    public PagoTransaccion(Integer idPago, Transaccion transaccion,
                           MetodoPago metodoPago, BigDecimal monto) {

        validarTransaccion(transaccion);
        validarMetodo(metodoPago);
        validarMonto(monto);

        this.idPago      = idPago;
        this.transaccion = transaccion;
        this.metodoPago  = metodoPago;
        this.monto       = monto.setScale(DbConstraints.PRECIO_SCALE,
                                          RoundingMode.HALF_UP);
    }

    public PagoTransaccion() {}

    /* ---------- Getters ---------- */
    public Integer     getIdPago()      { return idPago; }
    @Override
    public Integer getId() { return idPago; }
    public Transaccion getTransaccion() { return transaccion; }
    public MetodoPago  getMetodoPago()  { return metodoPago; }
    public BigDecimal  getMonto()       { return monto; }

    /* ---------- Setters con validaciones ---------- */

    public void setIdPago(Integer id) {
        requireIdNotSet(this.idPago, id,
                "El idPago ya fue asignado y no puede modificarse");
        this.idPago = id;
    }

    public void setTransaccion(Transaccion t) {
        validarTransaccion(t);
        this.transaccion = t;
    }

    public void setMetodoPago(MetodoPago m) {
        validarMetodo(m);
        this.metodoPago = m;
    }

    public void setMonto(BigDecimal monto) {
        validarMonto(monto);
        this.monto = monto.setScale(DbConstraints.PRECIO_SCALE,
                                   RoundingMode.HALF_UP);
    }

    /* ---------- Validaciones internas ---------- */

    private void validarTransaccion(Transaccion t) {
        requireNotNull(t, "La transacción es obligatoria en el pago");
    }

    private void validarMetodo(MetodoPago m) {
        requireNotNull(m, "El método de pago es obligatorio");
    }

    private void validarMonto(BigDecimal m) {
        requirePositive(m, "El monto del pago debe ser mayor a cero");
        requirePrecision(m, DbConstraints.PRECIO_PRECISION, DbConstraints.PRECIO_SCALE,
                "monto fuera de rango (" + DbConstraints.PRECIO_PRECISION + ',' + DbConstraints.PRECIO_SCALE + ")");
    }
}

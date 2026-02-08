package com.comercialvalerio.domain.model;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationUtils.*;
import java.time.LocalDateTime;

/*
 * Comprobante electrónico en PDF (boleta, factura).
 *
 * <p>Del DDL:</p>
 * <ul>
 *   <li>PK <code>idComprobante</code></li>
 *   <li>FK a <b>transaccion</b></li>
 *   <li>Restricción única <code>UNIQUE(idTransaccion)</code> &nbsp;
 *       (una transacción genera un solo comprobante).</li>
 * </ul>
 */
public class Comprobante extends BaseEntity<Integer> {

    private Integer       idComprobante;  // PK autogenerada
    private Transaccion   transaccion;    // obligatorio, único
    private LocalDateTime fechaEmision;   // no futura
    private byte[]        bytesPdf;       // no nulo, tamaño > 0

    /* ---------- Constructor con invariantes ---------- */
    public Comprobante(Integer idComprobante, Transaccion transaccion,
                       LocalDateTime fechaEmision, byte[] bytesPdf) {

        validarTransaccion(transaccion);
        validarFechaEmision(fechaEmision);
        validarPdf(bytesPdf);

        this.idComprobante = idComprobante;
        this.transaccion   = transaccion;
        this.fechaEmision  = fechaEmision;
        this.bytesPdf      = bytesPdf.clone();
    }

    public Comprobante() {}

    /* ---------- Getters ---------- */
    public Integer       getIdComprobante() { return idComprobante; }
    @Override
    public Integer getId() { return idComprobante; }
    public Transaccion   getTransaccion()   { return transaccion; }
    public LocalDateTime getFechaEmision()  { return fechaEmision; }
    public byte[]        getBytesPdf()      { return bytesPdf.clone(); }

    /* ---------- Setters con validaciones ---------- */

    public void setIdComprobante(Integer id) {
        requireIdNotSet(this.idComprobante, id,
                "El idComprobante ya fue asignado y no puede modificarse");
        this.idComprobante = id;
    }

    public void setTransaccion(Transaccion t) {
        validarTransaccion(t);
        this.transaccion = t;
    }

    public void setFechaEmision(LocalDateTime f) {
        validarFechaEmision(f);
        this.fechaEmision = f;
    }

    public void setBytesPdf(byte[] pdf) {
        validarPdf(pdf);
        this.bytesPdf = pdf.clone();
    }

    /* ---------- Validaciones internas ---------- */

    private void validarTransaccion(Transaccion t) {
        requireNotNull(t, "La transacción es obligatoria en el comprobante");
    }

    private void validarFechaEmision(LocalDateTime f) {
        if (f == null || f.isAfter(LocalDateTime.now()))
            throw new BusinessRuleViolationException(
                "La fecha de emisión no puede ser futura");
    }

    private void validarPdf(byte[] pdf) {
        if (pdf == null || pdf.length == 0)
            throw new BusinessRuleViolationException(
                "El PDF del comprobante no puede estar vacío");
    }
}

package com.comercialvalerio.infrastructure.persistence.entity;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity(name = "Comprobante")
@Table(name = "Comprobante")
@NamedQueries({
    @NamedQuery(name = "Comprobante.byTrans",
                query = "FROM Comprobante c WHERE c.transaccion.idTransaccion = :id"),
    @NamedQuery(name = "Comprobante.countByTransaccionExcludingId",
                query = "SELECT COUNT(c) FROM Comprobante c "
                      + "WHERE c.transaccion.idTransaccion = :tx "
                      + "AND (:id IS NULL OR c.idComprobante <> :id)")
})
public class ComprobanteEntity implements Serializable {
    /* ---------- PK ---------- */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idComprobante")
    private Integer idComprobante;
    /* ---------- Datos ---------- */
    @Column(name = "fechaEmision", nullable = false)
    private LocalDateTime fechaEmision;
    @Lob @Column(name = "bytesPdf", nullable = false)
    private byte[] bytesPdf;
    /* ---------- Relación uno-a-uno (única) ---------- */
    @OneToOne(optional = false) @JoinColumn(name = "idTransaccion")
    private TransaccionEntity transaccion;
    public ComprobanteEntity() {
    }
    public ComprobanteEntity(Integer idComprobante) {
        this.idComprobante = idComprobante;
    }
    public ComprobanteEntity(Integer idComprobante, LocalDateTime fechaEmision, byte[] bytesPdf) {
        this.idComprobante = idComprobante;
        this.fechaEmision = fechaEmision;
        this.bytesPdf = bytesPdf;
    }
    public Integer getIdComprobante() {
        return idComprobante;
    }
    public void setIdComprobante(Integer idComprobante) {
        this.idComprobante = idComprobante;
    }
    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }
    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
    public TransaccionEntity getTransaccion() {
        return transaccion;
    }
    public void setTransaccion(TransaccionEntity transaccion) {
        this.transaccion = transaccion;
    }
    public byte[] getBytesPdf() {
        return bytesPdf;
    }
    public void setBytesPdf(byte[] bytesPdf) {
        this.bytesPdf = bytesPdf;
    } 
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.Comprobante[ idComprobante=" + idComprobante + " ]";
    }
}

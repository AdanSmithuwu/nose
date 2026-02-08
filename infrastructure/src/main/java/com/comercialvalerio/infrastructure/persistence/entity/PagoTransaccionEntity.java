package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity(name = "PagoTransaccion")
@Table(name = "PagoTransaccion")
@NamedQueries({
    @NamedQuery(name = "PagoTransaccion.byTrans",
                query = "FROM PagoTransaccion p WHERE p.transaccion.idTransaccion = :id"),
    @NamedQuery(name = "PagoTransaccion.countByCliente",
                query = "SELECT COUNT(p) FROM PagoTransaccion p " +
                        "WHERE p.transaccion.cliente.idPersona = :idCli"),
    @NamedQuery(name = "PagoTransaccion.countByTransAndMetodo",
                query = "SELECT COUNT(p) FROM PagoTransaccion p " +
                        "WHERE p.transaccion.idTransaccion = :tx " +
                        "AND p.metodoPago.idMetodoPago = :met " +
                        "AND (:id IS NULL OR p.idPago <> :id)")
})
public class PagoTransaccionEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPago")
    private Integer idPago;
    @Column(name = "monto", nullable = false,
            precision = PRECIO_PRECISION, scale = PRECIO_SCALE)
    private BigDecimal monto;
    /* — relaciones — */
    @ManyToOne(optional = false) @JoinColumn(name = "idMetodoPago")
    private MetodoPagoEntity metodoPago;
    @ManyToOne(optional = false) @JoinColumn(name = "idTransaccion")
    private TransaccionEntity transaccion;
    public PagoTransaccionEntity() {
    }
    public PagoTransaccionEntity(Integer idPago) {
        this.idPago = idPago;
    }
    public PagoTransaccionEntity(Integer idPago, BigDecimal monto) {
        this.idPago = idPago;
        this.monto = monto;
    }
    public Integer getIdPago() {
        return idPago;
    }
    public void setIdPago(Integer idPago) {
        this.idPago = idPago;
    }
    public BigDecimal getMonto() {
        return monto;
    }
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
    public MetodoPagoEntity getMetodoPago() {
        return metodoPago;
    }
    public void setMetodoPago(MetodoPagoEntity metodoPago) {
        this.metodoPago = metodoPago;
    }
    public TransaccionEntity getTransaccion() {
        return transaccion;
    }
    public void setTransaccion(TransaccionEntity transaccion) {
        this.transaccion = transaccion;
    }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.PagoTransaccion[ idPago=" + idPago + " ]";
    }
}

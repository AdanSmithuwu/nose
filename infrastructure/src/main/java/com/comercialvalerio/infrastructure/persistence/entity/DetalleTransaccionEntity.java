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
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.Table;

@Entity(name = "DetalleTransaccion")
@Table(name = "DetalleTransaccion")
@NamedQueries({
    @NamedQuery(name = "DetalleTransaccion.byTrans",
                query = "FROM DetalleTransaccion d WHERE d.transaccion.idTransaccion = :id"),
    @NamedQuery(name = "DetalleTransaccion.countByProducto",
                query = "SELECT COUNT(d) FROM DetalleTransaccion d WHERE d.producto.idProducto = :prod")
})
public class DetalleTransaccionEntity implements Serializable {
    /* ---------- PK ---------- */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDetalle")
    private Integer idDetalle;
    /* ---------- Datos ---------- */
    @Column(name="cantidad",       nullable=false,
            precision=STOCK_PRECISION, scale=STOCK_SCALE)
    private BigDecimal cantidad;
    @Column(name="precioUnitario", nullable=false,
            precision=PRECIO_PRECISION, scale=PRECIO_SCALE)
    private BigDecimal precioUnitario;
    @Column(name="subtotal",
            precision=SUBTOTAL_PRECISION, scale=SUBTOTAL_SCALE,
            insertable=false, updatable=false)
    private BigDecimal subtotal;
    /* ---------- Relaciones ---------- */
    @ManyToOne(optional=false)      @JoinColumn(name="idProducto")
    private ProductoEntity   producto;
    @ManyToOne                     @JoinColumn(name="idTallaStock")
    private TallaStockEntity tallaStock;
    @ManyToOne(optional=false)      @JoinColumn(name="idTransaccion")
    private TransaccionEntity transaccion;
    public DetalleTransaccionEntity() {
    }
    public DetalleTransaccionEntity(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }
    public DetalleTransaccionEntity(Integer idDetalle, BigDecimal cantidad, BigDecimal precioUnitario) {
        this.idDetalle = idDetalle;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }
    public Integer getIdDetalle() {
        return idDetalle;
    }
    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }
    public BigDecimal getCantidad() {
        return cantidad;
    }
    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    public ProductoEntity getProducto() {
        return producto;
    }
    public void setProducto(ProductoEntity producto) {
        this.producto = producto;
    }
    public TallaStockEntity getTallaStock() {
        return tallaStock;
    }
    public void setTallaStock(TallaStockEntity tallaStock) {
        this.tallaStock = tallaStock;
    }
    public TransaccionEntity getTransaccion() {
        return transaccion;
    }
    public void setTransaccion(TransaccionEntity transaccion) {
        this.transaccion = transaccion;
    }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.DetalleTransaccion[ idDetalle=" + idDetalle + " ]";
    }
}

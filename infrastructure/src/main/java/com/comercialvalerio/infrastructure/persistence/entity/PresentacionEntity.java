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
import jakarta.persistence.UniqueConstraint;

@Entity(name = "Presentacion")
@Table(name = "Presentacion",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_presentacion_producto_cant",
           columnNames = {"idProducto","cantidad"}))
@NamedQueries({
    @NamedQuery(name = "Presentacion.byProducto",
                query = "FROM Presentacion p WHERE p.producto.idProducto = :p"),
    @NamedQuery(name = "Presentacion.countByProductoAndCantidad",
                query = "SELECT COUNT(p) FROM Presentacion p "
                      + "WHERE p.producto.idProducto = :prod "
                      + "AND p.cantidad = :cant "
                      + "AND (:id IS NULL OR p.idPresentacion <> :id)"),
    @NamedQuery(name = "Presentacion.enUso",
                query = "SELECT COUNT(d) FROM DetalleTransaccion d "
                      + "WHERE d.producto.idProducto = :prod "
                      + "AND d.tallaStock IS NULL "
                      + "AND d.producto = :prodEnt"),
    @NamedQuery(name = "Presentacion.updateEstado",
                query = "UPDATE Presentacion p "
                      + "SET p.estado = :e "
                      + "WHERE p.idPresentacion = :id"),
    @NamedQuery(name = "Presentacion.updateEstadoByProducto",
                query = "UPDATE Presentacion p "
                      + "SET p.estado = :e "
                      + "WHERE p.producto.idProducto = :pId"),
    @NamedQuery(name = "Presentacion.findByIds",
                query = "FROM Presentacion p WHERE p.idPresentacion IN :ids ORDER BY p.idPresentacion"),
    @NamedQuery(name = "Presentacion.findByProductos",
                query = "FROM Presentacion p WHERE p.producto.idProducto IN :ids ORDER BY p.producto.idProducto")
})
public class PresentacionEntity implements Serializable {
    /* ---------- PK ---------- */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPresentacion")
    private Integer idPresentacion;
    /* ---------- Datos ---------- */
    @Column(name = "cantidad", nullable = false,
            precision = CANTIDAD_PRECISION, scale = CANTIDAD_SCALE)
    private BigDecimal cantidad;
    @Column(name = "precio",   nullable = false,
            precision = PRECIO_PRECISION, scale = PRECIO_SCALE)
    private BigDecimal precio;
    /* ---------- FK al Producto y Estado ---------- */
    @ManyToOne(optional = false)
    @JoinColumn(name = "idProducto")
    private ProductoEntity producto;
    @ManyToOne(optional = false) @JoinColumn(name = "idEstado")
    private EstadoEntity estado;
    public PresentacionEntity() {
    }
    public PresentacionEntity(Integer idPresentacion) {
        this.idPresentacion = idPresentacion;
    }
    public PresentacionEntity(Integer idPresentacion, BigDecimal cantidad, BigDecimal precio, EstadoEntity estado) {
        this.idPresentacion = idPresentacion;
        this.cantidad = cantidad;
        this.precio = precio;
        this.estado = estado;
    }
    public Integer getIdPresentacion() {
        return idPresentacion;
    }
    public void setIdPresentacion(Integer idPresentacion) {
        this.idPresentacion = idPresentacion;
    }
    public BigDecimal getCantidad() {
        return cantidad;
    }
    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }
    public BigDecimal getPrecio() {
        return precio;
    }
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
    public ProductoEntity getProducto() {
        return producto;
    }
    public void setProducto(ProductoEntity producto) {
        this.producto = producto;
    }
    public EstadoEntity getEstado() {
        return estado;
    }
    public void setEstado(EstadoEntity estado) {
        this.estado = estado;
    }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.Presentacion[ idPresentacion=" + idPresentacion + " ]";
    }
}

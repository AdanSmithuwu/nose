package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity(name = "TallaStock")
@Table(name = "TallaStock",
       uniqueConstraints = @UniqueConstraint(name = "uk_talla_prod",
                                             columnNames = {"idProducto", "talla"}))
@NamedQueries({
    /*--- sólo las queries que realmente usa el repositorio ---*/
    @NamedQuery(name = "TallaStock.findByProducto",
                query = "FROM TallaStock t "
                      + "WHERE t.producto.idProducto = :idProd "
                      + "ORDER BY t.talla"),
    @NamedQuery(name = "TallaStock.findByProductoAndTalla",
                query = "FROM TallaStock t "
                      + "WHERE t.producto.idProducto = :idProd "
                      + "AND   UPPER(t.talla) = UPPER(:talla)"),
    @NamedQuery(name = "TallaStock.countDuplicados",
                query = "SELECT COUNT(t) FROM TallaStock t "
                      + "WHERE t.producto.idProducto = :p "
                      + "AND UPPER(t.talla) = :t "
                      + "AND (:id IS NULL OR t.idTallaStock <> :id)"),
    @NamedQuery(name = "TallaStock.updateEstado",
                query = "UPDATE TallaStock t "
                      + "SET t.estado = :e "
                      + "WHERE t.idTallaStock = :id"),
    @NamedQuery(name = "TallaStock.updateEstadoByProducto",
                query = "UPDATE TallaStock t "
                      + "SET t.estado = :e "
                      + "WHERE t.producto.idProducto = :p"),
    @NamedQuery(name = "TallaStock.findByIds",
                query = "FROM TallaStock t WHERE t.idTallaStock IN :ids ORDER BY t.idTallaStock"),
    @NamedQuery(name = "TallaStock.findByProductos",
                query = "FROM TallaStock t WHERE t.producto.idProducto IN :ids ORDER BY t.producto.idProducto, t.talla"),
    @NamedQuery(name = "TallaStock.stockById",
                query = "SELECT t.stock FROM TallaStock t WHERE t.idTallaStock = :id")
})
public class TallaStockEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTallaStock")
    private Integer idTallaStock;
    @Column(name = "talla", nullable = false, length = LEN_TALLA)
    private String  talla;
    @Column(name = "stock", nullable = false,
            precision = STOCK_PRECISION, scale = STOCK_SCALE)
    private BigDecimal stock;
    /* FK */
    @ManyToOne(optional = false) @JoinColumn(name = "idProducto")
    private ProductoEntity producto;
    @ManyToOne(optional = false) @JoinColumn(name = "idEstado")
    private EstadoEntity estado;
    /* Colecciones inversas si las necesitas */
    @OneToMany(mappedBy = "tallaStock")
    private Collection<MovimientoInventarioEntity> movimientos;
    @OneToMany(mappedBy = "tallaStock")
    private Collection<DetalleTransaccionEntity> detalles;
    public TallaStockEntity() {
    }
    public TallaStockEntity(Integer idTallaStock) {
        this.idTallaStock = idTallaStock;
    }
    public TallaStockEntity(Integer idTallaStock, String talla, BigDecimal stock, EstadoEntity estado) {
        this.idTallaStock = idTallaStock;
        this.talla = talla;
        this.stock = stock;
        this.estado = estado;
    }
    public Integer getIdTallaStock() {
        return idTallaStock;
    }
    public void setIdTallaStock(Integer idTallaStock) {
        this.idTallaStock = idTallaStock;
    }
    public String getTalla() {
        return talla;
    }
    public void setTalla(String talla) {
        this.talla = talla;
    }
    public BigDecimal getStock() {
        return stock;
    }
    public void setStock(BigDecimal stock) {
        this.stock = stock;
    }
    public Collection<MovimientoInventarioEntity> getMovimientos() {
        return movimientos;
    }
    public void setMovimientos(Collection<MovimientoInventarioEntity> movimientos) {
        this.movimientos = movimientos;
    }
    public Collection<DetalleTransaccionEntity> getDetalles() {
        return detalles;
    }
    public void setDetalles(Collection<DetalleTransaccionEntity> detalles) {
        this.detalles = detalles;
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
        return "com.comercialvalerio.infrastructure.persistence.entity.TallaStock[ idTallaStock=" + idTallaStock + " ]";
    }
}

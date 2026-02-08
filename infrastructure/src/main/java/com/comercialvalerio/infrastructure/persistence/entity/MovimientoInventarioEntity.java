package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity(name = "MovimientoInventario")
@Table(name = "MovimientoInventario",
       indexes = {
           @Index(name = "idx_mov_prod_fecha",  columnList = "idProducto,fechaHora"),
           @Index(name = "idx_mov_rango_fecha", columnList = "fechaHora")
       })
@NamedQueries({
    @NamedQuery(name = "MovimientoInventario.findAll",
                query = "SELECT m FROM MovimientoInventario m ORDER BY m.fechaHora DESC"),
    @NamedQuery(name = "MovimientoInventario.findByProducto",
                query = "SELECT m FROM MovimientoInventario m "
                      + "JOIN FETCH m.producto p "
                      + "JOIN FETCH p.estado "
                      + "JOIN FETCH m.empleado e "
                      + "JOIN FETCH e.persona per "
                      + "JOIN FETCH per.estado "
                      + "LEFT JOIN FETCH m.tallaStock ts "
                      + "JOIN FETCH m.tipoMovimiento "
                      + "WHERE m.producto.idProducto = :idProd "
                      + "ORDER BY m.fechaHora DESC"),
    @NamedQuery(name = "MovimientoInventario.findByRangoFecha",
                query = "SELECT m FROM MovimientoInventario m "
                      + "JOIN FETCH m.producto p "
                      + "JOIN FETCH p.estado "
                      + "JOIN FETCH m.empleado e "
                      + "JOIN FETCH e.persona per "
                      + "JOIN FETCH per.estado "
                      + "LEFT JOIN FETCH m.tallaStock ts "
                      + "JOIN FETCH m.tipoMovimiento "
                      + "WHERE m.fechaHora BETWEEN :desde AND :hasta "
                      + "ORDER BY m.fechaHora"),
    @NamedQuery(name = "MovimientoInventario.countByProducto",
                query = "SELECT COUNT(m) FROM MovimientoInventario m "
                      + "WHERE m.producto.idProducto = :idProd"),
    @NamedQuery(name = "MovimientoInventario.countByProductoAndMotivoNot",
                query = "SELECT COUNT(m) FROM MovimientoInventario m "
                      + "WHERE m.producto.idProducto = :idProd "
                      + "AND m.motivo <> :motivo"),
    @NamedQuery(name = "MovimientoInventario.countByTallaStockAndMotivoNot",
                query = "SELECT COUNT(m) FROM MovimientoInventario m "
                      + "WHERE m.tallaStock.idTallaStock = :idTall "
                      + "AND m.motivo <> :motivo"),
    @NamedQuery(name = "MovimientoInventario.countByEmpleado",
                query = "SELECT COUNT(m) FROM MovimientoInventario m "
                      + "WHERE m.empleado.idPersona = :idEmp")
})
public class MovimientoInventarioEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMovimiento")
    private Integer idMovimiento;
    @Column(name = "cantidad", nullable = false,
            precision = STOCK_PRECISION, scale = STOCK_SCALE)
    private BigDecimal cantidad;           // ≥ 0; el signo se infiere del tipo
    @Column(name = "motivo", length = LEN_MOTIVO)
    private String motivo;
    @Column(name = "fechaHora", nullable = false)
    private LocalDateTime fechaHora;
    /* ------------ FK ------------ */
    @ManyToOne(optional = false) @JoinColumn(name = "idEmpleado")
    private EmpleadoEntity    empleado;
    @ManyToOne(optional = false) @JoinColumn(name = "idProducto")
    private ProductoEntity    producto;
    @ManyToOne                 @JoinColumn(name = "idTallaStock")
    private TallaStockEntity   tallaStock;          // null ⇒ movimiento a nivel producto
    @ManyToOne(optional = false) @JoinColumn(name = "idTipoMovimiento")
    private TipoMovimientoEntity tipoMovimiento;
    public MovimientoInventarioEntity() {
    }
    public MovimientoInventarioEntity(Integer idMovimiento) {
        this.idMovimiento = idMovimiento;
    }
    public MovimientoInventarioEntity(Integer idMovimiento, BigDecimal cantidad, LocalDateTime fechaHora) {
        this.idMovimiento = idMovimiento;
        this.cantidad = cantidad;
        this.fechaHora = fechaHora;
    }
    public Integer getIdMovimiento() {
        return idMovimiento;
    }
    public void setIdMovimiento(Integer idMovimiento) {
        this.idMovimiento = idMovimiento;
    }
    public BigDecimal getCantidad() {
        return cantidad;
    }
    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }
    public String getMotivo() {
        return motivo;
    }
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
    public EmpleadoEntity getEmpleado() {
        return empleado;
    }
    public void setEmpleado(EmpleadoEntity empleado) {
        this.empleado = empleado;
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
    public TipoMovimientoEntity getTipoMovimiento() {
        return tipoMovimiento;
    }
    public void setTipoMovimiento(TipoMovimientoEntity tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.MovimientoInventario[ idMovimiento=" + idMovimiento + " ]";
    }
}

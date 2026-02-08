package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Cacheable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.time.LocalDateTime;

@Cacheable(false)

@Entity(name = "Transaccion")
@Table(name = "Transaccion",
       indexes = {
           @Index(name = "idx_trans_fecha" , columnList = "fecha"),
           @Index(name = "idx_trans_estado", columnList = "idEstado")
       })
@NamedQueries({
    @NamedQuery(name = "Transaccion.byId",
                query = "FROM Transaccion t WHERE t.idTransaccion = :id"),
    @NamedQuery(name = "Transaccion.countByEmpleado",
                query = "SELECT COUNT(t) FROM Transaccion t WHERE t.empleado.idPersona = :empId"),
    @NamedQuery(name = "Transaccion.countPedidosById",
                query = "SELECT COUNT(t) FROM Transaccion t "
                      + "WHERE t.idTransaccion = :idTx "
                      + "AND t.pedido IS NOT NULL"),
    @NamedQuery(name = "Transaccion.findByRangoFecha",
                query = "FROM Transaccion t WHERE t.fecha BETWEEN :d AND :h ORDER BY t.fecha"),
    @NamedQuery(name = "Transaccion.totalVentasCompletadas",
                query = "SELECT COALESCE(SUM(t.totalNeto),0) "
                      + "FROM Transaccion t JOIN t.estado e "
                      + "WHERE e.modulo='Transaccion' "
                      + "AND e.nombre IN ('Completada','Entregada')")
})
public class TransaccionEntity implements Serializable {
    /* ---------- Clave primaria ---------- */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTransaccion")
    private Integer idTransaccion;
    /* ---------- Datos básicos ---------- */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
    @Column(name = "totalBruto",   nullable = false,
            precision = PRECIO_PRECISION, scale = PRECIO_SCALE)
    private BigDecimal totalBruto;
    @Column(name = "descuento",    nullable = false,
            precision = PRECIO_PRECISION, scale = PRECIO_SCALE)
    private BigDecimal descuento;
    @Column(name = "cargo",        nullable = false,
            precision = PRECIO_PRECISION, scale = PRECIO_SCALE)
    private BigDecimal cargo;
    @Column(name = "totalNeto",    nullable = false,
            precision = PRECIO_PRECISION, scale = PRECIO_SCALE,
            insertable = false, updatable = false)
    private BigDecimal totalNeto;
    @Column(name = "observacion",  length = LEN_OBSERVACION)
    private String observacion;
    @Column(name = "motivoCancelacion", length = LEN_OBSERVACION)
    private String motivoCancelacion;
    /* ---------- Relaciones ---------- */
    @ManyToOne(optional = false) @JoinColumn(name = "idEmpleado")
    private EmpleadoEntity empleado;
    @ManyToOne(optional = false)
    @JoinColumn(name = "idCliente", nullable = false)
    private ClienteEntity  cliente;
    @ManyToOne(optional = false) @JoinColumn(name = "idEstado")
    private EstadoEntity   estado;
    /* Inversas (colecciones) */
    @OneToMany(mappedBy = "transaccion", cascade = CascadeType.ALL)
    private Collection<DetalleTransaccionEntity> detalles;
    @OneToMany(mappedBy = "transaccion", cascade = CascadeType.ALL)
    private Collection<PagoTransaccionEntity> pagos;
    /* ----- Sub-clases (lado inverso del 1:1) ----- */
    @OneToOne(mappedBy = "transaccion", cascade = CascadeType.ALL)
    private PedidoEntity pedido;
    @OneToOne(mappedBy = "transaccion", cascade = CascadeType.ALL)
    private VentaEntity  venta;
    @OneToOne(mappedBy = "transaccion", cascade = CascadeType.ALL)
    private ComprobanteEntity comprobante;
    /* ---------- Getters / setters ---------- */
    public Integer getIdTransaccion()            { return idTransaccion; }
    public void    setIdTransaccion(Integer id)  { this.idTransaccion = id; }
    public LocalDateTime getFecha()              { return fecha; }
    public void setFecha(LocalDateTime fecha)    { this.fecha = fecha; }
    public BigDecimal getTotalBruto()            { return totalBruto; }
    public void setTotalBruto(BigDecimal v)      { this.totalBruto = v; }
    public BigDecimal getDescuento()             { return descuento; }
    public void setDescuento(BigDecimal v)       { this.descuento = v; }
    public BigDecimal getCargo()                 { return cargo; }
    public void setCargo(BigDecimal v)           { this.cargo = v; }
    public BigDecimal getTotalNeto()             { return totalNeto; }
    public void setTotalNeto(BigDecimal v)       { this.totalNeto = v; }
    public String getObservacion()               { return observacion; }
    public void setObservacion(String obs)       { this.observacion = obs; }
    public String getMotivoCancelacion()         { return motivoCancelacion; }
    public void setMotivoCancelacion(String m)   { this.motivoCancelacion = m; }
    public EmpleadoEntity getEmpleado()          { return empleado; }
    public void setEmpleado(EmpleadoEntity e)    { this.empleado = e; }
    public ClienteEntity  getCliente()           { return cliente; }
    public void setCliente(ClienteEntity c)      { this.cliente = c; }
    public EstadoEntity   getEstado()            { return estado; }
    public void setEstado(EstadoEntity e)        { this.estado = e; }
    public Collection<DetalleTransaccionEntity> getDetalles() { return detalles; }
    public void setDetalles(Collection<DetalleTransaccionEntity> d) { detalles = d; }
    public Collection<PagoTransaccionEntity> getPagos() { return pagos; }
    public void setPagos(Collection<PagoTransaccionEntity> p) { pagos = p; }
    public PedidoEntity getPedido()                   { return pedido; }
    public void setPedido(PedidoEntity p)             { this.pedido = p; }
    public VentaEntity  getVenta()                    { return venta; }
    public void setVenta(VentaEntity v)               { this.venta = v; }
    public ComprobanteEntity getComprobante()         { return comprobante; }
    public void setComprobante(ComprobanteEntity c)   { this.comprobante = c; }   
    @Override public String toString() { return "TransaccionEntity[idTransaccion=" + idTransaccion + "]"; }
}

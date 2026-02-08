package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import jakarta.persistence.*;
import org.eclipse.persistence.annotations.ReadOnly;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@ReadOnly
@Entity(name = "HistorialTx")
@Table(name = "vw_HistorialTransaccionesPorCliente")
@NamedQueries({
    @NamedQuery(name = "HistorialTx.byCliente",
            query = "SELECT h FROM HistorialTx h WHERE h.idCliente = :id ORDER BY h.fecha DESC"),
    @NamedQuery(name = "HistorialTx.byClienteRange",
            query = "SELECT h FROM HistorialTx h WHERE h.idCliente = :id " +
                    "AND h.fecha BETWEEN :d AND :h ORDER BY h.fecha DESC")
})
public class HistorialTransaccionEntity implements Serializable {
    @Id
    @Column(name = "idTransaccion")
    private Integer idTransaccion;
    @Column(name = "idCliente")
    private Integer idCliente;
    @Column(name = "Cliente", length = LEN_NOMBRE_COMPLETO)
    private String cliente;
    @Column(name = "fecha")
    private LocalDateTime fecha;
    @Column(name = "totalNeto", precision = PRECIO_PRECISION, scale = PRECIO_SCALE)
    private BigDecimal totalNeto;
    @Column(name = "descuento", precision = PRECIO_PRECISION, scale = PRECIO_SCALE)
    private BigDecimal descuento;
    @Column(name = "cargo", precision = PRECIO_PRECISION, scale = PRECIO_SCALE)
    private BigDecimal cargo;
    @Column(name = "Estado", length = LEN_NOMBRE_CORTO)
    private String estado;
    @Column(name = "Tipo", length = LEN_NOMBRE_CORTO)
    private String tipo;
    public Integer getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(Integer idTransaccion) { this.idTransaccion = idTransaccion; }
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public BigDecimal getTotalNeto() { return totalNeto; }
    public void setTotalNeto(BigDecimal totalNeto) { this.totalNeto = totalNeto; }
    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }
    public BigDecimal getCargo() { return cargo; }
    public void setCargo(BigDecimal cargo) { this.cargo = cargo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}

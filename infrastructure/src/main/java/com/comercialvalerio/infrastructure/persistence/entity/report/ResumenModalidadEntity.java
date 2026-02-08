package com.comercialvalerio.infrastructure.persistence.entity.report;

import java.io.Serializable;
import java.math.BigDecimal;

import org.eclipse.persistence.annotations.ReadOnly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@ReadOnly
@Entity(name = "ResumenModalidad")
@Table(name = "vw_ResumenMensualModalidad")
@NamedQuery(name = "ResumenModalidad.byMes",
            query = "FROM ResumenModalidad r WHERE r.anio = :a AND r.mes = :m")
@IdClass(ResumenModalidadPK.class)
public class ResumenModalidadEntity implements Serializable {
    @Id
    @Column(name = "Anio")
    private Integer anio;
    @Id
    @Column(name = "Mes")
    private Integer mes;
    @Column(name = "NumTransMinorista")
    private Long numTransMinorista;
    @Column(name = "MontoMinorista")
    private BigDecimal montoMinorista;
    @Column(name = "NumTransEspecial")
    private Long numTransEspecial;
    @Column(name = "MontoEspecial")
    private BigDecimal montoEspecial;
    @Column(name = "NumPedidosDomicilio")
    private Long numPedidosDomicilio;
    @Column(name = "MontoPedidosDomicilio")
    private BigDecimal montoPedidosDomicilio;
    public Integer getAnio() { return anio; }
    public void setAnio(Integer a) { this.anio = a; }
    public Integer getMes() { return mes; }
    public void setMes(Integer m) { this.mes = m; }
    public Long getNumTransMinorista() { return numTransMinorista; }
    public void setNumTransMinorista(Long n) { this.numTransMinorista = n; }
    public BigDecimal getMontoMinorista() { return montoMinorista; }
    public void setMontoMinorista(BigDecimal m) { this.montoMinorista = m; }
    public Long getNumTransEspecial() { return numTransEspecial; }
    public void setNumTransEspecial(Long n) { this.numTransEspecial = n; }
    public BigDecimal getMontoEspecial() { return montoEspecial; }
    public void setMontoEspecial(BigDecimal m) { this.montoEspecial = m; }
    public Long getNumPedidosDomicilio() { return numPedidosDomicilio; }
    public void setNumPedidosDomicilio(Long n) { this.numPedidosDomicilio = n; }
    public BigDecimal getMontoPedidosDomicilio() { return montoPedidosDomicilio; }
    public void setMontoPedidosDomicilio(BigDecimal m) { this.montoPedidosDomicilio = m; }
}

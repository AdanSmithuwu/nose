package com.comercialvalerio.infrastructure.persistence.entity.report;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.persistence.annotations.ReadOnly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@ReadOnly
@Entity(name = "TransaccionesDia")
@Table(name = "vw_TransaccionesPorDia")
@NamedQuery(name = "TransaccionesDia.rango",
            query = "FROM TransaccionesDia t WHERE t.dia BETWEEN :d AND :h ORDER BY t.dia")
public class TransaccionesDiaEntity implements Serializable {
    @Id
    @Column(name = "Dia")
    private LocalDate dia;

    @Transient
    private Long numTransacciones;

    @Column(name = "NumTransacciones")
    private Long numVentas;

    @Column(name = "NumPedidosEntregados")
    private Long numPedidos;

    @Column(name = "TotalBrutoDia")
    private BigDecimal montoBruto;

    @Column(name = "TotalNetoDia")
    private BigDecimal montoNeto;

    @Column(name = "IngresosDia")
    private BigDecimal ingresosDia;
    public LocalDate getDia() { return dia; }
    public void setDia(LocalDate dia) { this.dia = dia; }
    public Long getNumTransacciones() {
        if (numTransacciones != null)
            return numTransacciones;
        long v = numVentas == null ? 0L : numVentas;
        long p = numPedidos == null ? 0L : numPedidos;
        return v + p;
    }
    public void setNumTransacciones(Long n) { this.numTransacciones = n; }
    public Long getNumVentas() { return numVentas; }
    public void setNumVentas(Long n) { this.numVentas = n; }
    public Long getNumPedidos() { return numPedidos; }
    public void setNumPedidos(Long n) { this.numPedidos = n; }
    public BigDecimal getMontoBruto() { return montoBruto; }
    public void setMontoBruto(BigDecimal b) { this.montoBruto = b; }
    public BigDecimal getMontoNeto() { return montoNeto; }
    public void setMontoNeto(BigDecimal n) { this.montoNeto = n; }
    public BigDecimal getIngresosDia() { return ingresosDia; }
    public void setIngresosDia(BigDecimal i) { this.ingresosDia = i; }
}

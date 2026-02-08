package com.comercialvalerio.infrastructure.persistence.entity.report;

import static com.comercialvalerio.common.DbConstraints.LEN_NOMBRE_CORTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import org.eclipse.persistence.annotations.ReadOnly;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@ReadOnly
@Entity(name = "PagoMetodoDia")
@Table(name = "vw_PagoMetodoDia")
@NamedQuery(name = "PagoMetodoDia.byFecha",
            query = "FROM PagoMetodoDia p WHERE p.dia = :d ORDER BY p.idMetodoPago")
@IdClass(PagoMetodoDiaPK.class)
public class PagoMetodoDiaEntity implements Serializable {
    @Id
    @Column(name = "Dia")
    private LocalDate dia;
    @Id
    @Column(name = "idMetodoPago")
    private Integer idMetodoPago;
    @Column(name = "Metodo", length = LEN_NOMBRE_CORTO)
    private String metodo;
    @Column(name = "Monto")
    private BigDecimal monto;

    public LocalDate getDia() { return dia; }
    public void setDia(LocalDate dia) { this.dia = dia; }
    public Integer getIdMetodoPago() { return idMetodoPago; }
    public void setIdMetodoPago(Integer id) { this.idMetodoPago = id; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String m) { this.metodo = m; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
}

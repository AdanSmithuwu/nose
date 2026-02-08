package com.comercialvalerio.infrastructure.persistence.entity.report;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
public class PagoMetodoDiaPK implements Serializable {
    @Column(name = "Dia")
    private LocalDate dia;
    @Column(name = "idMetodoPago")
    private Integer idMetodoPago;

    public LocalDate getDia() { return dia; }
    public void setDia(LocalDate d) { this.dia = d; }
    public Integer getIdMetodoPago() { return idMetodoPago; }
    public void setIdMetodoPago(Integer id) { this.idMetodoPago = id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PagoMetodoDiaPK that = (PagoMetodoDiaPK) o;
        return java.util.Objects.equals(dia, that.dia) &&
               java.util.Objects.equals(idMetodoPago, that.idMetodoPago);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(dia, idMetodoPago);
    }
}

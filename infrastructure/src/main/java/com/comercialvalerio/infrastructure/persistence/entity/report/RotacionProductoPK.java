package com.comercialvalerio.infrastructure.persistence.entity.report;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class RotacionProductoPK implements Serializable {
    @Column(name = "Anio")
    private Integer anio;
    @Column(name = "Mes")
    private Integer mes;
    @Column(name = "Posicion")
    private Integer posicion;

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }
    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }
    public Integer getPosicion() { return posicion; }
    public void setPosicion(Integer posicion) { this.posicion = posicion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RotacionProductoPK that = (RotacionProductoPK) o;
        return java.util.Objects.equals(anio, that.anio) &&
               java.util.Objects.equals(mes, that.mes) &&
               java.util.Objects.equals(posicion, that.posicion);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(anio, mes, posicion);
    }
}

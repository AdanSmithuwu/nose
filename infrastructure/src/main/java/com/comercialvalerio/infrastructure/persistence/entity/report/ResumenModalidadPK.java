package com.comercialvalerio.infrastructure.persistence.entity.report;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ResumenModalidadPK implements Serializable {
    @Column(name = "Anio")
    private Integer anio;
    @Column(name = "Mes")
    private Integer mes;

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }
    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResumenModalidadPK that = (ResumenModalidadPK) o;
        return java.util.Objects.equals(anio, that.anio) &&
               java.util.Objects.equals(mes, that.mes);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(anio, mes);
    }
}

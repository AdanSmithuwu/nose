package com.comercialvalerio.infrastructure.persistence.entity.report;

import static com.comercialvalerio.common.DbConstraints.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ResumenCategoriaPK implements Serializable {
    @Column(name = "Anio")
    private Integer anio;
    @Column(name = "Mes")
    private Integer mes;
    @Column(name = "Categoria", length = LEN_NOMBRE_CATEGORIA)
    private String categoria;

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }
    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResumenCategoriaPK that = (ResumenCategoriaPK) o;
        return java.util.Objects.equals(anio, that.anio) &&
               java.util.Objects.equals(mes, that.mes) &&
               java.util.Objects.equals(categoria, that.categoria);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(anio, mes, categoria);
    }
}

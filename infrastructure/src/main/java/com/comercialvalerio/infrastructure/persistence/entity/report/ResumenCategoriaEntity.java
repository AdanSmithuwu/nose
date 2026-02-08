package com.comercialvalerio.infrastructure.persistence.entity.report;

import static com.comercialvalerio.common.DbConstraints.*;

import jakarta.persistence.*;
import org.eclipse.persistence.annotations.ReadOnly;
import java.io.Serializable;
import java.math.BigDecimal;

@ReadOnly
@Entity(name = "ResumenCategoria")
@Table(name = "vw_ReporteMensualCategoria")
@NamedQuery(name = "ResumenCategoria.byMes",
            query = "FROM ResumenCategoria r WHERE r.anio = :a AND r.mes = :m")
@IdClass(ResumenCategoriaPK.class)
public class ResumenCategoriaEntity implements Serializable {
    @Id
    @Column(name = "Anio")
    private Integer anio;
    @Id
    @Column(name = "Mes")
    private Integer mes;
    @Id
    @Column(name = "Categoria", length = LEN_NOMBRE_CATEGORIA)
    private String categoria;
    @Column(name = "NumTransacciones")
    private Long numTransacciones;
    @Column(name = "IngresosCategoria")
    private BigDecimal ingresosCategoria;

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }
    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Long getNumTransacciones() { return numTransacciones; }
    public void setNumTransacciones(Long numTransacciones) { this.numTransacciones = numTransacciones; }
    public BigDecimal getIngresosCategoria() { return ingresosCategoria; }
    public void setIngresosCategoria(BigDecimal ingresosCategoria) { this.ingresosCategoria = ingresosCategoria; }
}

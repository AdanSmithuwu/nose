package com.comercialvalerio.infrastructure.persistence.entity.report;

import static com.comercialvalerio.common.DbConstraints.*;

import jakarta.persistence.*;
import org.eclipse.persistence.annotations.ReadOnly;
import java.io.Serializable;
import java.math.BigDecimal;

/** Clave compuesta por año, mes y posición */

@ReadOnly
@Entity(name = "RotacionProducto")
@Table(name = "vw_RotacionMensual")
@NamedNativeQuery(name = "RotacionProducto.byRango",
    query = "SELECT YEAR(?1) AS Anio, MONTH(?1) AS Mes, Posicion, " +
            "       idProducto, Producto, Categoria, TotalUnidadesVendidas, ImporteTotal " +
            "FROM (" +
            " SELECT ROW_NUMBER() OVER (ORDER BY SUM(Unidades) DESC) AS Posicion, " +
            "        idProducto, Producto, Categoria, SUM(Unidades) AS TotalUnidadesVendidas, " +
            "        SUM(Importe) AS ImporteTotal " +
            " FROM vw_RotacionRango " +
            " WHERE Dia >= ?1 AND Dia < ?2 " +
            " GROUP BY idProducto, Producto, Categoria" +
            ") R WHERE (?3 IS NULL OR Posicion <= ?3) ORDER BY Posicion",
    resultClass = RotacionProductoEntity.class)
@IdClass(RotacionProductoPK.class)
public class RotacionProductoEntity implements Serializable {
    @Id
    @Column(name = "Anio")
    private Integer anio;
    @Id
    @Column(name = "Mes")
    private Integer mes;
    @Id
    @Column(name = "Posicion")
    private Integer posicion;
    @Column(name = "idProducto")
    private Integer idProducto;
    @Column(name = "Producto", length = LEN_NOMBRE_PRODUCTO)
    private String producto;
    @Column(name = "TotalUnidadesVendidas")
    private BigDecimal totalUnidadesVendidas;
    @Column(name = "Categoria", length = LEN_NOMBRE_CATEGORIA)
    private String categoria;
    @Column(name = "ImporteTotal")
    private BigDecimal importeTotal;
    public Integer getAnio() { return anio; }
    public void setAnio(Integer a) { this.anio = a; }
    public Integer getMes() { return mes; }
    public void setMes(Integer m) { this.mes = m; }
    public Integer getPosicion() { return posicion; }
    public void setPosicion(Integer p) { this.posicion = p; }
    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer id) { this.idProducto = id; }
    public String getProducto() { return producto; }
    public void setProducto(String p) { this.producto = p; }
    public BigDecimal getTotalUnidadesVendidas() { return totalUnidadesVendidas; }
    public void setTotalUnidadesVendidas(BigDecimal u) { this.totalUnidadesVendidas = u; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String c) { this.categoria = c; }
    public BigDecimal getImporteTotal() { return importeTotal; }
    public void setImporteTotal(BigDecimal i) { this.importeTotal = i; }
}

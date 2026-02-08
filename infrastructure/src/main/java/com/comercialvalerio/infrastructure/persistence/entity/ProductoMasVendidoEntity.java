package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.LEN_NOMBRE_PRODUCTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import org.eclipse.persistence.annotations.ReadOnly;
import java.io.Serializable;
import java.math.BigDecimal;

@ReadOnly
@Entity(name = "ProductoMasVendido")
@Table(name = "vw_ProductosMasVendidos")
@NamedQuery(name = "ProductoMasVendido.top",
            query = "FROM ProductoMasVendido p ORDER BY p.unidadesVendidas DESC")
public class ProductoMasVendidoEntity implements Serializable {
    @Id
    @Column(name = "idProducto")
    private Integer idProducto;

    @Column(name = "nombre", length = LEN_NOMBRE_PRODUCTO)
    private String nombre;

    @Column(name = "UnidadesVendidas")
    private BigDecimal unidadesVendidas;

    @Column(name = "Ingresos")
    private BigDecimal ingresos;

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getUnidadesVendidas() { return unidadesVendidas; }
    public void setUnidadesVendidas(BigDecimal unidadesVendidas) { this.unidadesVendidas = unidadesVendidas; }
    public BigDecimal getIngresos() { return ingresos; }
    public void setIngresos(BigDecimal ingresos) { this.ingresos = ingresos; }
}

package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.Table;

@Entity(name = "ParametroSistema")
@Table(name = "ParametroSistema",
       indexes = @Index(name = "idx_parametro_clave", columnList = "clave"))
@NamedQueries({
    @NamedQuery(name = "ParametroSistema.findAll",
                query = "SELECT p FROM ParametroSistema p ORDER BY p.clave"),
    @NamedQuery(name = "ParametroSistema.findByClave",
                query = "SELECT p FROM ParametroSistema p WHERE p.clave = :clave")
})
@NamedNativeQuery(name = "ParametroSistema.decimal",
        query = "SELECT dbo.fn_GetParametroDecimal(?1, ?2)")
public class ParametroSistemaEntity implements Serializable {
    /* ---------- PK ---------- */
    @Id
    @Column(name = "clave", length = LEN_CLAVE_PARAM, nullable = false)      // DDL: NVARCHAR(30) NOT NULL
    private String clave;
    /* ---------- Datos ---------- */
    @Column(name = "valor", nullable = false,
            precision = PRECIO_PRECISION, scale = PRECIO_SCALE)
    private BigDecimal valor;
    @Column(name = "descripcion", length = LEN_DESCRIPCION)
    private String descripcion;
    @Column(name = "actualizado", nullable = false)
    private LocalDateTime actualizado;
    /* ---------- Relaciones ---------- */
    @ManyToOne(optional = false) @JoinColumn(name = "idEmpleado", nullable = false)
    private EmpleadoEntity empleado;
    public ParametroSistemaEntity() {
    }
    public ParametroSistemaEntity(String clave) {
        this.clave = clave;
    }
    public ParametroSistemaEntity(String clave, BigDecimal valor, LocalDateTime actualizado) {
        this.clave = clave;
        this.valor = valor;
        this.actualizado = actualizado;
    }
    public String getClave() {
        return clave;
    }
    public void setClave(String clave) {
        this.clave = clave;
    }
    public BigDecimal getValor() {
        return valor;
    }
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public LocalDateTime getActualizado() {
        return actualizado;
    }
    public void setActualizado(LocalDateTime actualizado) {
        this.actualizado = actualizado;
    }
    public EmpleadoEntity getEmpleado() {
        return empleado;
    }
    public void setEmpleado(EmpleadoEntity empleado) {
        this.empleado = empleado;
    }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.ParametroSistema[ clave=" + clave + " ]";
    }
}

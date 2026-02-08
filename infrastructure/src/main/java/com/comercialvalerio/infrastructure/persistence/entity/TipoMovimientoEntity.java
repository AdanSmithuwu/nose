package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import jakarta.persistence.CascadeType;
import java.io.Serializable;
import java.util.Collection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity(name = "TipoMovimiento")
@Table(name = "TipoMovimiento",
       uniqueConstraints = @UniqueConstraint(name = "uk_tipo_movimiento_nombre",
                                             columnNames = "nombre"))
@NamedQueries({
    @NamedQuery(name = "TipoMovimiento.findAll",
                query = "FROM TipoMovimiento t ORDER BY t.nombre"),
    @NamedQuery(name = "TipoMovimiento.findByNombre",
                query = "FROM TipoMovimiento t WHERE UPPER(t.nombre)=UPPER(:nombre)"),
    @NamedQuery(name = "TipoMovimiento.countByNombreExcludingId",
                query = "SELECT COUNT(t) FROM TipoMovimiento t WHERE UPPER(t.nombre)=:n AND (:id IS NULL OR t.idTipoMovimiento <> :id)")
})
public class TipoMovimientoEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTipoMovimiento")
    private Integer idTipoMovimiento;
    @Column(name = "nombre", nullable = false, length = LEN_NOMBRE_CORTO)
    private String  nombre;
    /* Relación inversa — sin cascade REMOVE para impedir borrado con histórico */
    @OneToMany(mappedBy="tipoMovimiento", cascade=CascadeType.ALL, orphanRemoval=true)
    private Collection<MovimientoInventarioEntity> movimientos;
    public TipoMovimientoEntity() {
    }
    public TipoMovimientoEntity(Integer idTipoMovimiento) {
        this.idTipoMovimiento = idTipoMovimiento;
    }
    public TipoMovimientoEntity(Integer idTipoMovimiento, String nombre) {
        this.idTipoMovimiento = idTipoMovimiento;
        this.nombre = nombre;
    }
    public Integer getIdTipoMovimiento() {
        return idTipoMovimiento;
    }
    public void setIdTipoMovimiento(Integer idTipoMovimiento) {
        this.idTipoMovimiento = idTipoMovimiento;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public Collection<MovimientoInventarioEntity> getMovimientos() {
        return movimientos;
    }
    public void setMovimientos(Collection<MovimientoInventarioEntity> movimientos) {
        this.movimientos = movimientos;
    }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.TipoMovimiento[ idTipoMovimiento=" + idTipoMovimiento + " ]";
    } 
}   

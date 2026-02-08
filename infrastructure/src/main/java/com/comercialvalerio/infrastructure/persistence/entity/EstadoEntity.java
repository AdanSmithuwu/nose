package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity(name = "Estado")
@Table(name = "Estado",
       uniqueConstraints = @UniqueConstraint(name = "uk_estado_modulo_nombre",
                                             columnNames = {"modulo","nombre"}))
@NamedQueries({
    @NamedQuery(name = "Estado.findAll",
                query = "FROM Estado e ORDER BY e.modulo, e.nombre"),
    @NamedQuery(name = "Estado.findByModuloAndNombre",
                query = "FROM Estado e WHERE UPPER(e.modulo)=UPPER(:m)"
                      + " AND UPPER(e.nombre)=UPPER(:n)"),
    @NamedQuery(name = "Estado.countByModuloNombreExcludingId",
                query = "SELECT COUNT(e) FROM Estado e "
                      + "WHERE UPPER(e.modulo)=:m AND UPPER(e.nombre)=:n "
                      + "AND (:id IS NULL OR e.idEstado <> :id)"),
    @NamedQuery(name = "Estado.nameById",
                query = "SELECT e.nombre FROM Estado e WHERE e.idEstado = :id"),
    @NamedQuery(name = "Transaccion.countByEstado",
                query = "SELECT COUNT(t) FROM Transaccion t "
                      + "WHERE t.estado.idEstado = :id")
})
public class EstadoEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstado")
    private Integer idEstado;  
    @Column(name = "nombre", nullable = false, length = LEN_NOMBRE_CORTO)
    private String nombre;
        @Column(name = "modulo", nullable = false, length = LEN_MODULO)
    private String modulo;
    public EstadoEntity() {
    }
    public EstadoEntity(Integer idEstado) {
        this.idEstado = idEstado;
    }
    public EstadoEntity(Integer idEstado, String nombre, String modulo) {
        this.idEstado = idEstado;
        this.nombre = nombre;
        this.modulo = modulo;
    }
    public Integer getIdEstado() {
        return idEstado;
    }
    public void setIdEstado(Integer idEstado) {
        this.idEstado = idEstado;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getModulo() {
        return modulo;
    }
    public void setModulo(String modulo) {
        this.modulo = modulo;
    }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.Estado[ idEstado=" + idEstado + " ]";
    }   
}

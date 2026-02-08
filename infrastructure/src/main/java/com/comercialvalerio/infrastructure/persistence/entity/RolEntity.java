package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import java.io.Serializable;
import java.util.Collection;
import jakarta.persistence.CascadeType;
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

@Entity(name = "Rol")
@Table(name = "Rol",
       uniqueConstraints = @UniqueConstraint(name = "uk_rol_nombre", columnNames = "nombre"))
@NamedQueries({
    @NamedQuery(name = "Rol.findAll",      query = "FROM Rol r ORDER BY r.nivel"),
    @NamedQuery(name = "Rol.findByNombre", query = "FROM Rol r WHERE UPPER(r.nombre) = UPPER(:nombre)"),
    @NamedQuery(name = "Rol.countEmpleados",
                query = "SELECT COUNT(e) FROM Empleado e WHERE e.rol.idRol = :id")
})
public class RolEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRol")
    private Integer idRol;  
    @Column(name = "nombre", nullable = false, length = LEN_NOMBRE_CORTO)
    private String nombre;
    @Column(name = "nivel", nullable = false)
    private short nivel;  
    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL)
    private Collection<EmpleadoEntity> empleadoCollection;
    public RolEntity() {
    }
    public RolEntity(Integer idRol) {
        this.idRol = idRol;
    }
    public RolEntity(Integer idRol, String nombre, short nivel) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.nivel = nivel;
    }
    public Integer getIdRol() {
        return idRol;
    }
    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public short getNivel() {
        return nivel;
    }
    public void setNivel(short nivel) {
        this.nivel = nivel;
    }
    public Collection<EmpleadoEntity> getEmpleadoCollection() {
        return empleadoCollection;
    }
    public void setEmpleadoCollection(Collection<EmpleadoEntity> empleadoCollection) {
        this.empleadoCollection = empleadoCollection;
    }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.Rol[ idRol=" + idRol + " ]";
    } 
}

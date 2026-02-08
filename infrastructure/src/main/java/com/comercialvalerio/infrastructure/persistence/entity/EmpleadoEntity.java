package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import java.io.Serializable;
import java.util.Collection;
import java.time.LocalDateTime;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedStoredProcedureQueries;
import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureParameter;
import jakarta.persistence.Table;

@Entity(name = "Empleado")
@Table(name = "Empleado",
       indexes = @Index(name = "uk_empleado_usuario",
                        columnList = "usuario", unique = true))
@NamedQueries({
   @NamedQuery(name="Empleado.findAll",
               query="SELECT e FROM Empleado e "
                     + "JOIN FETCH e.persona p "
                     + "JOIN FETCH p.estado "
                     + "JOIN FETCH e.rol"),
    @NamedQuery(name="Empleado.findByUsuario",
                query="SELECT e FROM Empleado e "
                      + "JOIN FETCH e.persona p "
                      + "JOIN FETCH p.estado "
                      + "JOIN FETCH e.rol "
                      + "WHERE e.usuario = :usuario"),
    @NamedQuery(name="Empleado.findByUsuarioLike",
                query="SELECT e FROM Empleado e WHERE LOWER(e.usuario) LIKE :patron"),
    @NamedQuery(name="Empleado.countByUsuario",
                query="SELECT COUNT(e) FROM Empleado e WHERE e.usuario = :u"),
    @NamedQuery(name="Empleado.findByIdFull",
                query="SELECT e FROM Empleado e "
                      + "JOIN FETCH e.persona p "
                      + "JOIN FETCH p.estado "
                      + "JOIN FETCH e.rol "
                      + "WHERE e.idPersona = :id"),
    @NamedQuery(name="Empleado.updatePersona",
                query="UPDATE Persona p "
                      + "SET p.nombres = :n, "
                      + "p.apellidos = :a, "
                      + "p.telefono = :t "
                      + "WHERE p.idPersona = :id"),
    @NamedQuery(name="Empleado.updateEmpleado",
                query="UPDATE Empleado e "
                      + "SET e.rol = :r, "
                      + "e.hashClave = :h, "
                      + "e.fechaCambioClave = CURRENT_TIMESTAMP, "
                      + "e.intentosFallidos = 0, "
                      + "e.bloqueadoHasta = null "
                      + "WHERE e.idPersona = :id"),
    @NamedQuery(name="Empleado.countTransacciones",
                query="SELECT COUNT(t) FROM Transaccion t "
                      + "WHERE t.empleado.idPersona = :id"),
    @NamedQuery(name="Empleado.resetClave",
                query="UPDATE Empleado e "
                      + "SET e.hashClave = :h, "
                      + "e.fechaCambioClave = CURRENT_TIMESTAMP, "
                      + "e.intentosFallidos = 0, "
                      + "e.bloqueadoHasta = null "
                      + "WHERE e.idPersona = :id"),
    @NamedQuery(name="Empleado.updateCredenciales",
                query="UPDATE Empleado e "
                      + "SET e.usuario = COALESCE(:u, e.usuario), "
                      + "e.hashClave = COALESCE(:h, e.hashClave), "
                      + "e.fechaCambioClave = CURRENT_TIMESTAMP, "
                      + "e.intentosFallidos = CASE WHEN :h IS NULL THEN e.intentosFallidos ELSE 0 END, "
                      + "e.bloqueadoHasta = CASE WHEN :h IS NULL THEN e.bloqueadoHasta ELSE null END "
                      + "WHERE e.idPersona = :id"),
    @NamedQuery(name="Empleado.updateSeguridad",
                query="UPDATE Empleado e "
                      + "SET e.intentosFallidos = :i, "
                      + "e.bloqueadoHasta = :b "
                      + "WHERE e.idPersona = :id"),
    @NamedQuery(name="Empleado.updateUltimoAcceso",
                query="UPDATE Empleado e "
                      + "SET e.ultimoAcceso = :f, "
                      + "e.intentosFallidos = 0, "
                      + "e.bloqueadoHasta = null "
                      + "WHERE e.idPersona = :id"),
    @NamedQuery(name="Empleado.findByIds",
                query="SELECT e FROM Empleado e "
                      + "JOIN FETCH e.persona p "
                      + "JOIN FETCH p.estado "
                      + "JOIN FETCH e.rol "
                      + "WHERE e.idPersona IN :ids"),
    @NamedQuery(name="Empleado.countActivoById",
                query="SELECT COUNT(e) FROM Empleado e "
                      + "JOIN e.persona p "
                      + "JOIN p.estado s "
                      + "WHERE e.idPersona = :id "
                      + "AND s.nombre = :estado")
})
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(name = "Empleado.registrar",
        procedureName = "sp_RegistrarEmpleado",
        parameters = {
            @StoredProcedureParameter(name = "nombres",       mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "apellidos",     mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "dni",           mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "telefono",      mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "fechaRegistro", mode = ParameterMode.IN,  type = java.sql.Date.class),
            @StoredProcedureParameter(name = "idEstado",      mode = ParameterMode.IN,  type = Integer.class),
            @StoredProcedureParameter(name = "usuario",       mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "hashClave",     mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "idRol",         mode = ParameterMode.IN,  type = Integer.class),
            @StoredProcedureParameter(name = "newIdPersona",  mode = ParameterMode.OUT, type = Integer.class)
        })
})
public class EmpleadoEntity implements Serializable {
    /* ---------- PK —-- también FK a Persona ---------- */
    @Id
    @Column(name = "idPersona")
    private Integer idPersona;
    @OneToOne(optional = false)
    @JoinColumn(name = "idPersona", insertable = false, updatable = false)
    private PersonaEntity persona;
    /* ---------- Credenciales ---------- */
    @Column(name = "usuario",   nullable = false, length = LEN_USUARIO)
    private String  usuario;
    @Column(name = "hashClave", nullable = false, length = LEN_HASH_CLAVE)
    private String  hashClave;              // hash + salt (Argon2)
    @Column(name = "fechaCambioClave", nullable = false)
    private LocalDateTime fechaCambioClave;
    /* ---------- Seguridad ---------- */
    @Column(name = "ultimoAcceso")
    private LocalDateTime ultimoAcceso;
    @Column(name = "intentosFallidos", nullable = false)
    private int     intentosFallidos;
    @Column(name = "bloqueadoHasta")
    private LocalDateTime bloqueadoHasta;
    /* ---------- Relaciones ---------- */
    @ManyToOne(optional = false) @JoinColumn(name = "idRol")
    private RolEntity rol;
    @OneToMany(mappedBy = "empleado",   cascade = CascadeType.ALL) private Collection<TransaccionEntity>   transacciones;
    @OneToMany(mappedBy = "empleado")                               private Collection<ReporteEntity>       reportes;
    @OneToMany(mappedBy = "empleado")                               private Collection<MovimientoInventarioEntity> movimientos;
    @OneToMany(mappedBy = "empleado")                               private Collection<ParametroSistemaEntity>    parametros;
    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL)    private Collection<BitacoraLoginEntity>       bitacoras;
    public EmpleadoEntity() {
    }
    public EmpleadoEntity(Integer idPersona) {
        this.idPersona = idPersona;
    }
    public EmpleadoEntity(Integer idPersona, String usuario, String hashClave, int intentosFallidos) {
        this.idPersona = idPersona;
        this.usuario = usuario;
        this.hashClave = hashClave;
        this.intentosFallidos = intentosFallidos;
    }
    public Integer getIdPersona() {
        return idPersona;
    }
    public void setIdPersona(Integer idPersona) {
        this.idPersona = idPersona;
    }
    public String getUsuario() {
        return usuario;
    }
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
    public String getHashClave() {
        return hashClave;
    }
    public void setHashClave(String hashClave) {
        this.hashClave = hashClave;
    }
    public LocalDateTime getFechaCambioClave() {
        return fechaCambioClave;
    }
    public void setFechaCambioClave(LocalDateTime fechaCambioClave) {
        this.fechaCambioClave = fechaCambioClave;
    }
    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }
    public int getIntentosFallidos() {
        return intentosFallidos;
    }
    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }
    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }
    public PersonaEntity getPersona() {
        return persona;
    }
    public void setPersona(PersonaEntity persona) {
        this.persona = persona;
    }
    public RolEntity getRol() {
        return rol;
    }
    public void setRol(RolEntity rol) {
        this.rol = rol;
    }
    public Collection<TransaccionEntity> getTransacciones() {
        return transacciones;
    }
    public void setTransacciones(Collection<TransaccionEntity> transacciones) {
        this.transacciones = transacciones;
    }
    public Collection<ReporteEntity> getReportes() {
        return reportes;
    }
    public void setReportes(Collection<ReporteEntity> reportes) {
        this.reportes = reportes;
    }
    public Collection<MovimientoInventarioEntity> getMovimientos() {
        return movimientos;
    }
    public void setMovimientos(Collection<MovimientoInventarioEntity> movimientos) {
        this.movimientos = movimientos;
    }
    public Collection<ParametroSistemaEntity> getParametros() {
        return parametros;
    }
    public void setParametros(Collection<ParametroSistemaEntity> parametros) {
        this.parametros = parametros;
    }
    public Collection<BitacoraLoginEntity> getBitacoras() {
        return bitacoras;
    }
    public void setBitacoras(Collection<BitacoraLoginEntity> bitacoras) {
        this.bitacoras = bitacoras;
    }   
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.Empleado[ idPersona=" + idPersona + " ]";
    }
}

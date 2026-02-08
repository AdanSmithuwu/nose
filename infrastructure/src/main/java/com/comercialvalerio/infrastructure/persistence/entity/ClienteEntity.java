package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import java.io.Serializable;
import java.util.Collection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedStoredProcedureQueries;
import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.StoredProcedureParameter;
import jakarta.persistence.Table;

@Entity(name = "Cliente")
@Table(name = "Cliente")
@NamedQueries({
    @NamedQuery(name="Cliente.findAll",
                query="SELECT c FROM Cliente c"),
    @NamedQuery(name="Cliente.findByDni",
                query="SELECT c FROM Cliente c WHERE c.persona.dni = :dni"),
    @NamedQuery(name="Cliente.findByNombreLike",
                query="SELECT c FROM Cliente c "
                     + "WHERE UPPER(c.persona.nombres) LIKE :patron "
                     + "   OR UPPER(c.persona.apellidos) LIKE :patron"),
    @NamedQuery(name="Cliente.findByTelefono",
                query="SELECT c FROM Cliente c WHERE c.persona.telefono = :tel"),
    @NamedQuery(name="Cliente.activosByNombre",
                query="SELECT c, "
                      + "FUNCTION('dbo.fn_NombreCompleto', c.idPersona) "
                      + "FROM Cliente c "
                      + "WHERE c.idPersona IN (SELECT a.idPersona FROM ClienteActivo a) "
                      + "AND UPPER(FUNCTION('dbo.fn_NombreCompleto', c.idPersona)) LIKE :patron"),
    @NamedQuery(name="Cliente.activosByTelefono",
                query="SELECT c, "
                      + "FUNCTION('dbo.fn_NombreCompleto', c.idPersona) "
                      + "FROM Cliente c "
                      + "WHERE c.idPersona IN (SELECT a.idPersona FROM ClienteActivo a) "
                      + "AND c.persona.telefono = :tel"),
    @NamedQuery(name="Cliente.activosByRangoRegistro",
                query="SELECT c FROM Cliente c "
                      + "WHERE c.idPersona IN (SELECT a.idPersona FROM ClienteActivo a) "
                      + "AND c.persona.fechaRegistro BETWEEN :d AND :h"),
    @NamedQuery(name="Cliente.activos",
                query="SELECT c FROM Cliente c WHERE c.idPersona IN (SELECT a.idPersona FROM ClienteActivo a)"),
    @NamedQuery(name="Cliente.byEstado",
                query="SELECT c FROM Cliente c WHERE UPPER(c.persona.estado.nombre)=:estado"),
    @NamedQuery(name="Cliente.updateDireccion",
                query="UPDATE Cliente c SET c.direccion = :dir WHERE c.idPersona = :id")
})
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(name = "Cliente.registrar",
        procedureName = "dbo.sp_RegistrarCliente",
        parameters = {
            @StoredProcedureParameter(name = "nombres",      mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "apellidos",    mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "dni",          mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "telefono",     mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "direccion",    mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "idEstado",     mode = ParameterMode.IN,  type = Integer.class),
            @StoredProcedureParameter(name = "newIdPersona", mode = ParameterMode.OUT, type = Integer.class)
        })
})
public class ClienteEntity implements Serializable {
    /* ---------- PK (FK a Persona) ---------- */
    @Id
    @Column(name = "idPersona")
    private Integer idPersona;
    @Column(name = "direccion", length = LEN_DIRECCION, nullable = false)
    private String  direccion;
    @OneToOne(optional = false)
    @JoinColumn(name = "idPersona", insertable = false, updatable = false)
    private PersonaEntity persona;
    /* ---------- Relaciones ---------- */
    @OneToMany(mappedBy = "cliente")
    private Collection<TransaccionEntity> transacciones;
    public ClienteEntity() {
    }
    public ClienteEntity(Integer idPersona) {
        this.idPersona = idPersona;
    }
    public Integer getIdPersona() {
        return idPersona;
    }
    public void setIdPersona(Integer idPersona) {
        this.idPersona = idPersona;
    }
    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    public Collection<TransaccionEntity> getTransacciones() {
        return transacciones;
    }
    public void setTransacciones(Collection<TransaccionEntity> transacciones) {
        this.transacciones = transacciones;
    }
    public PersonaEntity getPersona() {
        return persona;
    }
    public void setPersona(PersonaEntity persona) {
        this.persona = persona;
    }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.Cliente[ idPersona=" + idPersona + " ]";
    }
}

package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import java.io.Serializable;
import java.time.LocalDate;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/*
 * Entidad JPA que representa a una Persona (base para Cliente y Empleado).
 */
@Entity(name = "Persona")
@Table(name = "Persona",
       indexes = {
           // Índice único sobre DNI para búsquedas y restricción de unicidad en BD
           @Index(name = "uk_persona_dni", columnList = "dni", unique = true)
       })
@NamedQueries({
    @NamedQuery(name = "Persona.findByDni",
                query = "SELECT p FROM Persona p WHERE p.dni = :dni"),
    @NamedQuery(name = "Persona.updateDatos",
                query = "UPDATE Persona p "
                      + "SET p.nombres = :n, "
                      + "p.apellidos = :a, "
                      + "p.dni = :d, "
                      + "p.telefono = :t "
                      + "WHERE p.idPersona = :id"),
    @NamedQuery(name = "Persona.countByDniExcludingId",
                query = "SELECT COUNT(p) FROM Persona p "
                      + "WHERE p.dni = :dni "
                      + "AND (:id IS NULL OR p.idPersona <> :id)")
})
public class PersonaEntity implements Serializable {
    /* ---------- Clave primaria ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPersona")
    private Integer idPersona;
    /* ---------- Datos básicos ---------- */
    @Column(name = "nombres",   nullable = false, length = LEN_NOMBRE_PERSONA)
    private String  nombres;
    @Column(name = "apellidos", nullable = false, length = LEN_NOMBRE_PERSONA)
    private String  apellidos;
    @Column(name = "dni",       nullable = false, columnDefinition = DEF_DNI_CHAR)
    private String  dni;  // único (índice ↑)
    @Column(name = "telefono",  length = LEN_TELEFONO)
    private String  telefono;
    @Column(name = "fechaRegistro", nullable = false)
    private LocalDate fechaRegistro;
    /* ---------- Relaciones ---------- */
    @ManyToOne(optional = false)
    @JoinColumn(name = "idEstado")
    private EstadoEntity   estado;
    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL)
    private EmpleadoEntity empleado;
    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL)
    private ClienteEntity  cliente;
    /* ---------- Constructores ---------- */
    public PersonaEntity() { }
    public PersonaEntity(Integer idPersona) {
        this.idPersona = idPersona;
    }
    public PersonaEntity(Integer idPersona,
                         String nombres,
                         String apellidos,
                         String dni,
                         LocalDate fechaRegistro) {
        this.idPersona     = idPersona;
        this.nombres       = nombres;
        this.apellidos     = apellidos;
        this.dni           = dni;
        this.fechaRegistro = fechaRegistro;
    }
    /* ---------- Getters / Setters ---------- */
    public Integer getIdPersona()               { return idPersona; }
    public void    setIdPersona(Integer id)     { this.idPersona = id; }
    public String  getNombres()                 { return nombres; }
    public void    setNombres(String n)         { this.nombres = n; }
    public String  getApellidos()               { return apellidos; }
    public void    setApellidos(String a)       { this.apellidos = a; }
    public String  getDni()                     { return dni; }
    public void    setDni(String d)             { this.dni = d; }
    public String  getTelefono()                { return telefono; }
    public void    setTelefono(String t)        { this.telefono = t; }
    public LocalDate getFechaRegistro()         { return fechaRegistro; }
    public void     setFechaRegistro(LocalDate f) { this.fechaRegistro = f; }
    public EstadoEntity   getEstado()          { return estado; }
    public void            setEstado(EstadoEntity e) { this.estado = e; }
    public EmpleadoEntity getEmpleado()         { return empleado; }
    public void           setEmpleado(EmpleadoEntity e) { this.empleado = e; }
    public ClienteEntity  getCliente()          { return cliente; }
    public void           setCliente(ClienteEntity c)  { this.cliente = c; }
    @Override public String toString() { return "PersonaEntity[idPersona=" + idPersona + "]"; }
}

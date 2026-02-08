package com.comercialvalerio.infrastructure.persistence.entity;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedStoredProcedureQueries;
import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureParameter;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity(name = "BitacoraLogin")
@Table(name = "BitacoraLogin",
       indexes = {
         @Index(name = "idx_bitacora_fecha", columnList = "fechaEvento"),
         @Index(name = "idx_bitacora_emp",   columnList = "idEmpleado")
       })
@NamedQueries({
    @NamedQuery(name = "BitacoraLogin.byEmpleado",
                query = "FROM BitacoraLogin b WHERE b.empleado.idPersona = :idEmpleado"),
    @NamedQuery(name = "BitacoraLogin.byRangoFecha",
                query = "FROM BitacoraLogin b WHERE b.fechaEvento BETWEEN :desde AND :hasta ORDER BY b.fechaEvento"),
    @NamedQuery(name = "BitacoraLogin.byRangoFechaExitoso",
                query = "FROM BitacoraLogin b WHERE b.fechaEvento BETWEEN :desde AND :hasta "
                      + "AND (:exitoso IS NULL OR b.exitoso = :exitoso) "
                      + "ORDER BY b.fechaEvento"),
    @NamedQuery(name = "BitacoraLogin.countByEmpleado",
                query = "SELECT COUNT(b) FROM BitacoraLogin b WHERE b.empleado.idPersona = :idEmpleado")
})
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(name = "BitacoraLogin.depurarAntiguos",
        procedureName = "dbo.sp_DepurarBitacoraLogin",
        parameters = {
            @StoredProcedureParameter(name = "maxFecha",    mode = ParameterMode.IN,  type = LocalDateTime.class),
            @StoredProcedureParameter(name = "rowsDeleted", mode = ParameterMode.OUT, type = Integer.class)
        })
})
public class BitacoraLoginEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBitacora")
    private Integer idBitacora;
    @Column(name = "fechaEvento", nullable = false)
    private LocalDateTime fechaEvento;
    @Column(name = "exitoso", nullable = false)
    private boolean exitoso;
    @ManyToOne(optional = false)
    @JoinColumn(name = "idEmpleado")
    private EmpleadoEntity empleado;
    public BitacoraLoginEntity() {
    }
    public BitacoraLoginEntity(Integer idBitacora) {
        this.idBitacora = idBitacora;
    }
    public BitacoraLoginEntity(Integer idBitacora, LocalDateTime fechaEvento, boolean exitoso) {
        this.idBitacora = idBitacora;
        this.fechaEvento = fechaEvento;
        this.exitoso = exitoso;
    }
    public Integer getIdBitacora() {
        return idBitacora;
    }
    public void setIdBitacora(Integer idBitacora) {
        this.idBitacora = idBitacora;
    }
    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }
    public void setFechaEvento(LocalDateTime fechaEvento) {
        this.fechaEvento = fechaEvento;
    }
    public boolean getExitoso() {
        return exitoso;
    }
    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }
    public EmpleadoEntity getEmpleado() {
        return empleado;
    }
    public void setEmpleado(EmpleadoEntity empleado) {
        this.empleado = empleado;
    }   
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.BitacoraLogin[ idBitacora=" + idBitacora + " ]";
    }
}

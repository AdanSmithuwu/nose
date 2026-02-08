package com.comercialvalerio.infrastructure.persistence.entity;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.NamedStoredProcedureQueries;
import jakarta.persistence.StoredProcedureParameter;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity(name = "Venta")
@Table(name = "Venta")
@NamedQueries({
    @NamedQuery(name = "Venta.findAll",
                query = "SELECT v FROM Venta v"),
    @NamedQuery(name = "Venta.findByIdTransaccion",
                query = "SELECT v FROM Venta v WHERE v.idTransaccion = :id"),
    @NamedQuery(name = "Venta.findByRangoFecha",
                query = "SELECT v FROM Venta v "
                      + "WHERE v.transaccion.fecha BETWEEN :d AND :h"),
    @NamedQuery(name = "Venta.findByCliente",
                query = "SELECT v FROM Venta v "
                      + "WHERE v.transaccion.cliente.idPersona = :idCli"),
    @NamedQuery(name = "Venta.countById",
                query = "SELECT COUNT(v) FROM Venta v WHERE v.idTransaccion = :id")
})
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(name = "Venta.registrar",
        procedureName = "dbo.sp_RegistrarVenta",
        parameters = {
            @StoredProcedureParameter(name = "idEmpleado",     mode = ParameterMode.IN,  type = Integer.class),
            @StoredProcedureParameter(name = "idCliente",      mode = ParameterMode.IN,  type = Integer.class),
            @StoredProcedureParameter(name = "observacion",    mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "detalle",        mode = ParameterMode.IN,  type = Object.class),
            @StoredProcedureParameter(name = "pagos",          mode = ParameterMode.IN,  type = Object.class),
            @StoredProcedureParameter(name = "idTransaccion",  mode = ParameterMode.OUT, type = Integer.class)
        }),
    @NamedStoredProcedureQuery(name = "Venta.cancelar",
        procedureName = "dbo.sp_CancelarVenta",
        parameters = {
            @StoredProcedureParameter(name = "idTransaccion",     mode = ParameterMode.IN, type = Integer.class),
            @StoredProcedureParameter(name = "motivoCancelacion", mode = ParameterMode.IN, type = String.class)
        })
})
public class VentaEntity implements Serializable {
   /* ---------- PK compartida con Transaccion ---------- */
    @Id @Column(name = "idTransaccion")
    private Integer idTransaccion;
    /* Relación 1-a-1 (dueño = TransaccionEntity)   */
    @OneToOne(optional = false) @MapsId
    @JoinColumn(name = "idTransaccion")
    private TransaccionEntity transaccion;
    public VentaEntity() {
    }
    public VentaEntity(Integer idTransaccion) {
        this.idTransaccion = idTransaccion;
    }
    public Integer getIdTransaccion() {
        return idTransaccion;
    }
    public void setIdTransaccion(Integer idTransaccion) {
        this.idTransaccion = idTransaccion;
    }
    public TransaccionEntity getTransaccion() {
        return transaccion;
    }
    public void setTransaccion(TransaccionEntity transaccion) {
        this.transaccion = transaccion;
    }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.Venta[ idTransaccion=" + idTransaccion + " ]";
    } 
}

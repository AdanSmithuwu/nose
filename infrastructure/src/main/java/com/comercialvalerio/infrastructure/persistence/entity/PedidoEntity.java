package com.comercialvalerio.infrastructure.persistence.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import static com.comercialvalerio.common.DbConstraints.LEN_DIRECCION;
import static com.comercialvalerio.common.DbConstraints.LEN_OBSERVACION;
import static com.comercialvalerio.common.DbConstraints.LEN_TIPO_PEDIDO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.NamedStoredProcedureQueries;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.StoredProcedureParameter;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity(name = "Pedido")
@Table(name = "Pedido")
@NamedQueries({
    @NamedQuery(name = "Pedido.findAll",
                query = "SELECT p FROM Pedido p"),
    @NamedQuery(name = "Pedido.findByIdTransaccion",
                query = "SELECT p FROM Pedido p WHERE p.idTransaccion = :idTransaccion"),
    @NamedQuery(name = "Pedido.findPendientesEntrega",
                query = "SELECT p FROM Pedido p WHERE p.transaccion.estado.nombre = 'En Proceso'")
    ,
    @NamedQuery(name = "Pedido.totalPedidos",
                query = "SELECT COUNT(p) FROM Pedido p"),
    @NamedQuery(name = "Pedido.findByRangoFecha",
                query = "SELECT p FROM Pedido p "
                      + "WHERE p.transaccion.fecha BETWEEN :d AND :h")
    ,
    @NamedQuery(name = "Pedido.existsByCliente",
                query = "SELECT COUNT(p) FROM Pedido p WHERE p.transaccion.cliente.idPersona = :cli"),
    @NamedQuery(name = "Pedido.existsByEmpleadoEntrega",
                query = "SELECT COUNT(p) FROM Pedido p WHERE p.empleadoEntrega.idPersona = :emp")
})
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(name = "Pedido.registrar",
        procedureName = "dbo.sp_RegistrarPedido",
        parameters = {
            @StoredProcedureParameter(name = "idEmpleado",       mode = ParameterMode.IN,  type = Integer.class),
            @StoredProcedureParameter(name = "idCliente",        mode = ParameterMode.IN,  type = Integer.class),
            @StoredProcedureParameter(name = "observacion",      mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "direccionEntrega", mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "usaValeGas",       mode = ParameterMode.IN,  type = Boolean.class),
            @StoredProcedureParameter(name = "cargo",            mode = ParameterMode.IN,  type = java.math.BigDecimal.class),
            @StoredProcedureParameter(name = "detalle",          mode = ParameterMode.IN,  type = Object.class),
            @StoredProcedureParameter(name = "idTransaccion",    mode = ParameterMode.OUT, type = Integer.class)
        }),
    @NamedStoredProcedureQuery(name = "Pedido.actualizarEstado",
        procedureName = "dbo.sp_ActualizarEstadoPedido",
        parameters = {
            @StoredProcedureParameter(name = "idTransaccion",     mode = ParameterMode.IN, type = Integer.class),
            @StoredProcedureParameter(name = "nuevoEstado",       mode = ParameterMode.IN, type = String.class),
            @StoredProcedureParameter(name = "comentario",        mode = ParameterMode.IN, type = String.class),
            @StoredProcedureParameter(name = "fechaHoraEntrega",  mode = ParameterMode.IN, type = java.time.LocalDateTime.class),
            @StoredProcedureParameter(name = "idEmpleadoEntrega", mode = ParameterMode.IN, type = Integer.class)
        }),
    @NamedStoredProcedureQuery(name = "Pedido.modificar",
        procedureName = "dbo.sp_ModificarPedido",
        parameters = {
            @StoredProcedureParameter(name = "idTransaccion",    mode = ParameterMode.IN, type = Integer.class),
            @StoredProcedureParameter(name = "observacion",      mode = ParameterMode.IN, type = String.class),
            @StoredProcedureParameter(name = "direccionEntrega", mode = ParameterMode.IN, type = String.class),
            @StoredProcedureParameter(name = "usaValeGas",       mode = ParameterMode.IN, type = Boolean.class),
            @StoredProcedureParameter(name = "cargo",            mode = ParameterMode.IN, type = java.math.BigDecimal.class),
            @StoredProcedureParameter(name = "detalle",          mode = ParameterMode.IN, type = Object.class)
        }),
    @NamedStoredProcedureQuery(name = "Pedido.agregarPagos",
        procedureName = "dbo.sp_AgregarPagosTransaccion",
        parameters = {
            @StoredProcedureParameter(name = "idTransaccion", mode = ParameterMode.IN, type = Integer.class),
            @StoredProcedureParameter(name = "pagos",         mode = ParameterMode.IN, type = Object.class)
        }),
    @NamedStoredProcedureQuery(name = "Pedido.pendientesEntrega",
        procedureName = "dbo.sp_ListarPedidosPendientes",
        resultSetMappings = "PedidoPendienteMapping")
})
@SqlResultSetMapping(name = "PedidoPendienteMapping",
    classes = @ConstructorResult(targetClass = com.comercialvalerio.infrastructure.persistence.dto.PedidoPendienteDto.class,
        columns = {
            @ColumnResult(name = "idTransaccion", type = Integer.class),
            @ColumnResult(name = "fecha", type = java.time.LocalDateTime.class),
            @ColumnResult(name = "idEmpleado", type = Integer.class),
            @ColumnResult(name = "idCliente", type = Integer.class),
            @ColumnResult(name = "direccionEntrega", type = String.class),
            @ColumnResult(name = "tipoPedido", type = String.class),
            @ColumnResult(name = "usaValeGas", type = Boolean.class)
        }))
public class PedidoEntity implements Serializable {
    /* ========  PK compartida con Transaccion  ======== */
    @Id
    @Column(name = "idTransaccion")
    private Integer idTransaccion;
    /* Dueño de la relación – hereda la PK de Transaccion */
    @OneToOne(optional = false) @MapsId
    @JoinColumn(name = "idTransaccion")
    private TransaccionEntity transaccion;
    /* ========  Campos propios  ======== */
    @Column(name = "direccionEntrega", nullable = false, length = LEN_DIRECCION)
    private String  direccionEntrega;
    @Column(name = "fechaHoraEntrega")
    private LocalDateTime fechaHoraEntrega;
    /* CHECK en BD: ('Domicilio','Especial') */
    @Column(name = "tipoPedido",       nullable = false, length = LEN_TIPO_PEDIDO)
    private String  tipoPedido;
    @Column(name = "usaValeGas",       nullable = false)
    private boolean usaValeGas;
    @Column(name = "comentarioCancelacion", length = LEN_OBSERVACION)
    private String  comentarioCancelacion;
    @ManyToOne
    @JoinColumn(name = "idEmpleadoEntrega")
    private EmpleadoEntity empleadoEntrega;
    public PedidoEntity() {
    }
    public PedidoEntity(Integer idTransaccion) {
        this.idTransaccion = idTransaccion;
    }
    public PedidoEntity(Integer idTransaccion, String direccionEntrega, String tipoPedido, boolean usaValeGas) {
        this.idTransaccion = idTransaccion;
        this.direccionEntrega = direccionEntrega;
        this.tipoPedido = tipoPedido;
        this.usaValeGas = usaValeGas;
    }
    public Integer getIdTransaccion() {
        return idTransaccion;
    }
    public void setIdTransaccion(Integer idTransaccion) {
        this.idTransaccion = idTransaccion;
    }
    public String getDireccionEntrega() {
        return direccionEntrega;
    }
    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }
    public String getTipoPedido() {
        return tipoPedido;
    }
    public void setTipoPedido(String tipoPedido) {
        this.tipoPedido = tipoPedido;
    }
    public boolean getUsaValeGas() {
        return usaValeGas;
    }
    public void setUsaValeGas(boolean usaValeGas) {
        this.usaValeGas = usaValeGas;
    }
    public String getComentarioCancelacion() {
        return comentarioCancelacion;
    }
    public void setComentarioCancelacion(String comentarioCancelacion) {
        this.comentarioCancelacion = comentarioCancelacion;
    }
    public LocalDateTime getFechaHoraEntrega() {
        return fechaHoraEntrega;
    }
    public void setFechaHoraEntrega(LocalDateTime fechaHoraEntrega) {
        this.fechaHoraEntrega = fechaHoraEntrega;
    }
    public EmpleadoEntity getEmpleadoEntrega() {
        return empleadoEntrega;
    }
    public void setEmpleadoEntrega(EmpleadoEntity empleadoEntrega) {
        this.empleadoEntrega = empleadoEntrega;
    }
    public TransaccionEntity getTransaccion() {
        return transaccion;
    }
    public void setTransaccion(TransaccionEntity transaccion) {
        this.transaccion = transaccion;
    }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.Pedido[ idTransaccion=" + idTransaccion + " ]";
    }
}

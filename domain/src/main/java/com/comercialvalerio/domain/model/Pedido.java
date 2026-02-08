package com.comercialvalerio.domain.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;

/*
 * Pedido con entrega diferida (Domicilio o Especial).
 *
 * Reglas adicionales sobre {@link Transaccion}:
 * <ul>
 *   <li>Debe indicarse <code>direccionEntrega</code>.</li>
 *   <li><code>tipoPedido</code> = “Domicilio” | “Especial” (DDL enum).</li>
 *   <li>Si estado = Cancelada → se requiere <code>comentarioCancelacion</code>
 *       (regla de negocio propia del flujo de pedidos).</li>
 * </ul>
 */
public class Pedido extends Transaccion {

    private String     direccionEntrega;    // obligatorio
    private TipoPedido tipoPedido;          // Domicilio o Especial
    private boolean    usaValeGas;          // indicador
    private String     comentarioCancelacion; // si Cancelada
    private LocalDateTime fechaHoraEntrega;   // hora real de entrega
    private Integer       idEmpleadoEntrega;  // empleado que entregó

    /* ---------- Constructor completo con invariantes ---------- */
    public Pedido(String direccionEntrega,
                  TipoPedido tipoPedido, boolean usaValeGas,
                  String comentarioCancelacion,
                  Integer idTransaccion, LocalDateTime fecha, Estado estado,
                  BigDecimal totalBruto, BigDecimal descuento,
                  BigDecimal cargo, BigDecimal totalNeto,
                  String observacion, String motivoCancelacion,
                  Empleado empleado, Cliente cliente,
                  LocalDateTime fechaHoraEntrega, Integer idEmpleadoEntrega) {

        super(idTransaccion, fecha, estado, totalBruto, descuento, cargo,
              totalNeto, observacion, motivoCancelacion, empleado, cliente);

        validarDireccion(direccionEntrega);
        validarTipoPedido(tipoPedido);
        validarComentarioCancelacion(comentarioCancelacion, estado);
        validarFechaHoraEntrega(fechaHoraEntrega);
        validarEmpleadoEntrega(idEmpleadoEntrega);

        this.direccionEntrega     = direccionEntrega.trim();
        this.tipoPedido           = tipoPedido;
        this.usaValeGas           = usaValeGas;
        this.comentarioCancelacion= comentarioCancelacion == null
                                    ? null
                                    : comentarioCancelacion.trim();
        this.fechaHoraEntrega   = fechaHoraEntrega;
        this.idEmpleadoEntrega  = idEmpleadoEntrega;
    }

    /* Constructor simplificado: se completan luego campos de Transaccion. */
    public Pedido(String direccionEntrega,
                  TipoPedido tipoPedido, boolean usaValeGas,
                  String comentarioCancelacion) {
        this(direccionEntrega, tipoPedido, usaValeGas,
             comentarioCancelacion, null, null, null,
             null, null, null, null, null, null, null, null,
             null, null);
    }

    public Pedido() {}

    /* ---------- Getters & Setters con validaciones ---------- */

    public String getDireccionEntrega() { return direccionEntrega; }
    public void   setDireccionEntrega(String direccionEntrega) {
        validarDireccion(direccionEntrega);
        this.direccionEntrega = direccionEntrega.trim();
    }

    public TipoPedido getTipoPedido() { return tipoPedido; }
    public void   setTipoPedido(TipoPedido tipoPedido) {
        validarTipoPedido(tipoPedido);
        this.tipoPedido = tipoPedido;
    }

    public boolean isUsaValeGas() { return usaValeGas; }
    public void setUsaValeGas(boolean usaValeGas) { this.usaValeGas = usaValeGas; }

    public String getComentarioCancelacion() { return comentarioCancelacion; }
    public void   setComentarioCancelacion(String comentarioCancelacion) {
        validarComentarioCancelacion(comentarioCancelacion, getEstado());
        this.comentarioCancelacion = comentarioCancelacion == null
                                     ? null
                                     : comentarioCancelacion.trim();
    }

    public LocalDateTime getFechaHoraEntrega() { return fechaHoraEntrega; }
    public void setFechaHoraEntrega(LocalDateTime f) {
        validarFechaHoraEntrega(f);
        this.fechaHoraEntrega = f;
    }

    public Integer getIdEmpleadoEntrega() { return idEmpleadoEntrega; }
    public void setIdEmpleadoEntrega(Integer id) {
        validarEmpleadoEntrega(id);
        this.idEmpleadoEntrega = id;
    }

    /* ---------- Reglas de dominio ---------- */

    /**
     * Cambia el estado del pedido a Cancelada validando la transición.
     * El estado actual debe ser "En Proceso" y el nuevo estado debe
     * corresponder a "Cancelada" en el módulo Transaccion.
     */
    public void cancelar(Estado cancelada, String motivo) {
        if (!EstadoNombre.EN_PROCESO.equalsNombre(getEstado().getNombre()))
            throw new BusinessRuleViolationException(
                    "Sólo pedidos en estado 'En Proceso' pueden cancelarse (estado actual: "
                    + getEstado().getNombre() + ")");
        if (cancelada == null
                || !"Transaccion".equalsIgnoreCase(cancelada.getModulo())
                || !EstadoNombre.CANCELADA.equalsNombre(cancelada.getNombre()))
            throw new BusinessRuleViolationException("Estado inválido para cancelación");
        if (motivo == null || motivo.isBlank())
            throw new BusinessRuleViolationException(
                    "Debe indicar el motivo de cancelación");

        setMotivoCancelacion(motivo);
        setEstado(cancelada);
        setComentarioCancelacion(motivo);
    }

    /** Valida que el pedido pueda marcarse como entregado y asocia los pagos. */
    public void marcarEntregado(List<PagoTransaccion> pagos) {
        if (!EstadoNombre.EN_PROCESO.equalsNombre(getEstado().getNombre()))
            throw new BusinessRuleViolationException(
                    "Sólo pedidos en 'En Proceso' pueden entregarse (actual: "
                    + getEstado().getNombre() + ")");
        if (pagos != null) {
            for (PagoTransaccion pago : pagos) {
                if (pago == null) continue;
                pago.setTransaccion(this);
                agregarPago(pago);
            }
        }
        validarTotalPagado();
    }

    /**
     * Completa el pedido registrando los pagos y asignando el estado
     * "Entregada".
     */
    public void completar(Estado entregada, List<PagoTransaccion> pagos) {
        if (entregada == null
                || !"Transaccion".equalsIgnoreCase(entregada.getModulo())
                || !EstadoNombre.ENTREGADA.equalsNombre(entregada.getNombre()))
            throw new BusinessRuleViolationException("Estado inválido para completar pedido");
        marcarEntregado(pagos);
        setEstado(entregada);
    }

    /* ---------- Validaciones internas ---------- */

    private void validarDireccion(String d) {
        if (d == null || d.isBlank() || d.length() > DbConstraints.LEN_DIRECCION)
            throw new BusinessRuleViolationException(
                "La dirección de entrega es obligatoria (máx. " + DbConstraints.LEN_DIRECCION + " caracteres)");
    }

    private void validarTipoPedido(TipoPedido t) {
        if (t == null)
            throw new BusinessRuleViolationException(
                "El tipo de pedido es obligatorio");
    }

    private void validarComentarioCancelacion(String c, Estado e) {
        boolean cancelado = e != null && EstadoNombre.CANCELADA.equalsNombre(e.getNombre());
        if (cancelado && (c == null || c.isBlank()))
            throw new BusinessRuleViolationException(
                "Debe indicar comentarioCancelacion cuando el pedido está cancelado");
        if (!cancelado && c != null)
            throw new BusinessRuleViolationException(
                "comentarioCancelacion solo aplica si el pedido está cancelado");
        if (c != null && c.length() > DbConstraints.LEN_OBSERVACION)
            throw new BusinessRuleViolationException(
                "El comentario de cancelación supera " + DbConstraints.LEN_OBSERVACION + " caracteres");
    }

    private void validarFechaHoraEntrega(LocalDateTime f) {
        if (f == null) return;
        if (f.isAfter(LocalDateTime.now()))
            throw new BusinessRuleViolationException(
                "La fecha y hora de entrega no puede ser futura");
        LocalDateTime creacion = getFecha();
        if (creacion != null && f.isBefore(creacion))
            throw new BusinessRuleViolationException(
                "La fecha y hora de entrega no puede ser anterior a la transacción");
    }

    private void validarEmpleadoEntrega(Integer id) {
        if (id == null)
            throw new BusinessRuleViolationException(
                "Debe registrarse el empleado que entregó el pedido");
    }
}

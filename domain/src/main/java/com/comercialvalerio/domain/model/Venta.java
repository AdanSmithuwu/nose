package com.comercialvalerio.domain.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.comercialvalerio.domain.exception.BusinessRuleViolationException;

/*
 * Venta de mostrador o mayorista.
 *
 * <p>No agrega campos adicionales; todas las invariantes
 * (fechas, totales, estados, motivoCancelacion, etc.)
 * se validan en {@link Transaccion}.</p>
 */
public class Venta extends Transaccion {

    public Venta(Integer idTransaccion,                      // PK
                 LocalDateTime fecha,                        // fecha de emisión
                 Estado estado,                              // Completada, Cancelada…
                 BigDecimal totalBruto,
                 BigDecimal descuento,
                 BigDecimal cargo,
                 BigDecimal totalNeto,
                 String observacion,
                 String motivoCancelacion,
                 Empleado empleado,                          // cajero
                 Cliente cliente) {                          // opcional
        super(idTransaccion, fecha, estado, totalBruto, descuento, cargo,
              totalNeto, observacion, motivoCancelacion, empleado, cliente);
    }

    /* Constructor vacío requerido por frameworks de serialización/JPA. */
    public Venta() {}

    /**
     * Cambia el estado de la venta a Cancelada validando la transición.
     * Sólo aplica cuando la venta está en estado "Completada".
     */
    public void cancelar(Estado cancelada, String motivo) {
        if (!EstadoNombre.COMPLETADA.getNombre().equalsIgnoreCase(getEstado().getNombre()))
            throw new BusinessRuleViolationException(
                    "Sólo ventas en estado 'Completada' pueden cancelarse (estado actual: "
                    + getEstado().getNombre() + ")");
        if (cancelada == null
                || !"Transaccion".equalsIgnoreCase(cancelada.getModulo())
                || !EstadoNombre.CANCELADA.getNombre().equalsIgnoreCase(cancelada.getNombre()))
            throw new BusinessRuleViolationException("Estado inválido para cancelación");
        if (motivo == null || motivo.isBlank())
            throw new BusinessRuleViolationException(
                    "Debe indicar el motivo de cancelación");

        setMotivoCancelacion(motivo);
        setEstado(cancelada);
    }

    /**
     * Completa la venta asignando el estado "Completada".  Pensado
     * para ventas que pudieran crearse en estado "En Proceso".
     */
    public void completar(Estado completada) {
        if (!EstadoNombre.EN_PROCESO.getNombre().equalsIgnoreCase(getEstado().getNombre()))
            throw new BusinessRuleViolationException(
                    "Sólo ventas en 'En Proceso' pueden completarse (actual: "
                    + getEstado().getNombre() + ")");
        if (completada == null
                || !"Transaccion".equalsIgnoreCase(completada.getModulo())
                || !EstadoNombre.COMPLETADA.getNombre().equalsIgnoreCase(completada.getNombre()))
            throw new BusinessRuleViolationException("Estado inválido para completar venta");
        setEstado(completada);
    }
}

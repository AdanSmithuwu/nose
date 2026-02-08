package com.comercialvalerio.infrastructure.persistence.impl;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.domain.model.Transaccion;
import com.comercialvalerio.domain.repository.TransaccionRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.TransaccionEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.TransaccionMapper;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.StoredProcedureQuery;

@ApplicationScoped
public class TransaccionRepositoryImpl extends BaseRepository implements TransaccionRepository {
@Inject
    TransaccionMapper mapper;

    /* ---------- CONSULTA ---------- */
    @Override
    public java.util.Optional<Transaccion> findById(Integer id) {
        return readOptional(em -> {
            TransaccionEntity ent = em.find(TransaccionEntity.class, id);
            return ent == null ? null : mapper.toDomain(ent);
        });
    }

    @Override
    public List<Transaccion> findByRangoFecha(LocalDateTime desde, LocalDateTime hasta) {
        return read(em -> map(
                em.createNamedQuery("Transaccion.findByRangoFecha", TransaccionEntity.class)
                        .setParameter("d", desde)
                        .setParameter("h", hasta),
                mapper::toDomain));
    }

    /* ----------   ESTADOS   -------------------------------------------------
       Se unifica la lógica usando el SP dbo.sp_ActualizarEstadoPedido para
       *cualquier* transacción.  Si la transacción es Venta se redirige a
       dbo.sp_CancelarVenta (única operación válida sobre una venta cerrada).
       ---------------------------------------------------------------------*/
    @Override
    public void actualizarEstado(Integer idTx, Integer idEstado, String motivo) {
        tx(em -> {
            boolean esPedido = em.createNamedQuery(
                            "Transaccion.countPedidosById", Long.class)
                    .setParameter("idTx", idTx)
                    .getSingleResult() > 0;

            /*-----------------------------------------------------------
              Para simplificar invocamos siempre con el NOMBRE del estado
              y dejamos que la base valide la transición.               
            -----------------------------------------------------------*/
            String nombreEstado = em.createNamedQuery("Estado.nameById", String.class)
                    .setParameter("id", idEstado)
                    .getSingleResult();

            if (esPedido) {
                StoredProcedureQuery sp = em.createNamedStoredProcedureQuery("Pedido.actualizarEstado");
                sp.setParameter("idTransaccion", idTx);
                sp.setParameter("nuevoEstado", nombreEstado);
                sp.setParameter("comentario", motivo);
                sp.setParameter("fechaHoraEntrega", null);
                sp.setParameter("idEmpleadoEntrega", null);
                sp.execute();
            } else if ("Cancelada".equalsIgnoreCase(nombreEstado)) {
                StoredProcedureQuery sp = em.createNamedStoredProcedureQuery("Venta.cancelar");
                sp.setParameter("idTransaccion", idTx);
                sp.setParameter("motivoCancelacion", motivo);
                sp.execute();
            } else {
                throw new BusinessRuleViolationException("Solo se admite cambio de estado en Pedidos o cancelación de Ventas");
            }
            return null;
        });
    }

    @Override
    public boolean existsByEmpleado(Integer idEmpleado) {
        return read(em -> em.createNamedQuery("Transaccion.countByEmpleado", Long.class)
                .setParameter("empId", idEmpleado)
                .getSingleResult() > 0);
    }
}

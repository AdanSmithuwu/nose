package com.comercialvalerio.infrastructure.persistence.impl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.comercialvalerio.domain.model.PagoTransaccion;
import com.comercialvalerio.domain.model.Pedido;
import com.comercialvalerio.domain.repository.PedidoRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.PedidoEntity;
import com.comercialvalerio.infrastructure.persistence.entity.TransaccionEntity;
import com.comercialvalerio.infrastructure.persistence.entity.ClienteEntity;
import com.comercialvalerio.infrastructure.persistence.entity.EstadoEntity;
import com.comercialvalerio.infrastructure.persistence.entity.EmpleadoEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.PedidoMapper;
import com.comercialvalerio.infrastructure.persistence.tvp.SqlServerTvpBuilder;
import com.comercialvalerio.infrastructure.transaction.TransactionManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.StoredProcedureQuery;

@ApplicationScoped
public class PedidoRepositoryImpl extends BaseRepository implements PedidoRepository {
@Inject
    PedidoMapper mapper;

    /* ---------- LECTURA (sin cambios) ---------- */
    @Override
    public List<Pedido> findAll() {
        return read(em -> map(
                em.createNamedQuery("Pedido.findAll", PedidoEntity.class)
                        .setHint("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
                        .setHint("jakarta.persistence.cache.storeMode", CacheStoreMode.BYPASS),
                mapper::toDomain));
    }

    @Override
    public java.util.Optional<Pedido> findById(Integer id) {
        return readOptional(em -> em.createNamedQuery("Pedido.findByIdTransaccion", PedidoEntity.class)
                .setParameter("idTransaccion", id)
                .setHint("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
                .setHint("jakarta.persistence.cache.storeMode", CacheStoreMode.BYPASS)
                .getResultStream()
                .findFirst()
                .map(mapper::toDomain)
                .orElse(null));
    }

    @Override
    public List<Pedido> findPendientesEntrega() {
        return read(em -> {
            StoredProcedureQuery sp = em.createNamedStoredProcedureQuery("Pedido.pendientesEntrega");
            boolean more = sp.execute();
            List<Pedido> lista = new ArrayList<>();
            if (more) {
                var rows = BaseRepository.resultList(sp,
                        com.comercialvalerio.infrastructure.persistence.dto.PedidoPendienteDto.class);
                for (var r : rows) {
                    PedidoEntity pe = new PedidoEntity();
                    pe.setIdTransaccion(r.idTransaccion());
                    pe.setDireccionEntrega(r.direccionEntrega());
                    pe.setTipoPedido(r.tipoPedido());
                    pe.setUsaValeGas(Boolean.TRUE.equals(r.usaValeGas()));

                    TransaccionEntity tx = new TransaccionEntity();
                    tx.setIdTransaccion(r.idTransaccion());
                    tx.setFecha(r.fecha());

                    EmpleadoEntity emp = em.find(EmpleadoEntity.class, r.idEmpleado());
                    ClienteEntity cli = em.find(ClienteEntity.class, r.idCliente());

                    tx.setEmpleado(emp);
                    tx.setCliente(cli);
                    tx.setEstado(new EstadoEntity(null, "En Proceso", "Transaccion"));

                    pe.setTransaccion(tx);
                    lista.add(mapper.toDomain(pe));
                }
            }
            return lista;
        });
    }

    @Override
    public List<Pedido> findByRangoFecha(LocalDateTime d, LocalDateTime h) {
        LocalDateTime desde = d == null
                ? LocalDateTime.of(1, 1, 1, 0, 0)
                : d;
        LocalDateTime hasta = h == null
                ? LocalDateTime.of(9999, 12, 31, 23, 59, 59)
                : h;
        return read(em -> map(
                em.createNamedQuery("Pedido.findByRangoFecha", PedidoEntity.class)
                        .setParameter("d", desde)
                        .setParameter("h", hasta)
                        .setHint("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
                        .setHint("jakarta.persistence.cache.storeMode", CacheStoreMode.BYPASS),
                mapper::toDomain));
    }

    @Override
    public boolean existsByCliente(Integer idCliente) {
        return read(em -> em.createNamedQuery("Pedido.existsByCliente", Long.class)
                .setParameter("cli", idCliente)
                .getSingleResult() > 0);
    }

    @Override
    public boolean existsByEmpleadoEntrega(Integer idEmpleado) {
        return read(em -> em.createNamedQuery("Pedido.existsByEmpleadoEntrega", Long.class)
                .setParameter("emp", idEmpleado)
                .getSingleResult() > 0);
    }

    /* ---------- ESCRITURA ---------- */
    @Override
    public void save(Pedido p) {
        tx(em -> {
            StoredProcedureQuery sp = em.createNamedStoredProcedureQuery("Pedido.registrar");
            sp.setParameter("idEmpleado", p.getEmpleado().getIdPersona());
            sp.setParameter("idCliente", p.getCliente() == null ? null : p.getCliente().getIdPersona());
            sp.setParameter("observacion", p.getObservacion());
            sp.setParameter("direccionEntrega", p.getDireccionEntrega());
            sp.setParameter("usaValeGas", p.isUsaValeGas());
            sp.setParameter("cargo", p.getCargo());
            try {
                sp.setParameter("detalle", SqlServerTvpBuilder.detalle(p.getDetalles()));
                sp.execute();
            } catch (java.sql.SQLException ex) {
                throw translate(ex);
            }
            p.setIdTransaccion((Integer) sp.getOutputParameterValue("idTransaccion"));
            return null;
        });
    }

    @Override
    public void update(Pedido p) {
        tx(em -> {
            StoredProcedureQuery sp = em.createNamedStoredProcedureQuery("Pedido.modificar");
            sp.setParameter("idTransaccion", p.getIdTransaccion());
            sp.setParameter("observacion", p.getObservacion());
            sp.setParameter("direccionEntrega", p.getDireccionEntrega());
            sp.setParameter("usaValeGas", p.isUsaValeGas());
            sp.setParameter("cargo", p.getCargo());
            try {
                sp.setParameter("detalle", SqlServerTvpBuilder.detalle(p.getDetalles()));
                sp.execute();
            } catch (java.sql.SQLException ex) {
                throw translate(ex);
            }
            return null;
        });
    }

    @Override
    public void cancelar(Integer idPedido, String motivo) {
        tx(em -> {
            StoredProcedureQuery sp = em.createNamedStoredProcedureQuery("Pedido.actualizarEstado");
            sp.setParameter("idTransaccion", idPedido);
            sp.setParameter("nuevoEstado", "Cancelada");
            sp.setParameter("comentario", motivo);
            sp.setParameter("fechaHoraEntrega", null);
            sp.setParameter("idEmpleadoEntrega", null);
            sp.execute();
            return null;
        });
    }

    @Override
    public void marcarEntregado(Integer idPedido, List<PagoTransaccion> pagos,
                                LocalDateTime fechaHoraEntrega, Integer idEmpleadoEntrega) {
        tx(em -> {
            // 1) Registro de los pagos adicionales
            StoredProcedureQuery spPagos = em.createNamedStoredProcedureQuery("Pedido.agregarPagos");
            spPagos.setParameter("idTransaccion", idPedido);
            try {
                spPagos.setParameter("pagos", SqlServerTvpBuilder.pagos(pagos));
                spPagos.execute();
            } catch (java.sql.SQLException ex) {
                throw translate(ex);
            }

            // 2) Cambio de estado por SP
            StoredProcedureQuery spEstado = em.createNamedStoredProcedureQuery("Pedido.actualizarEstado");
            spEstado.setParameter("idTransaccion", idPedido);
            spEstado.setParameter("nuevoEstado", "Entregada");
            spEstado.setParameter("comentario", null);
            spEstado.setParameter("fechaHoraEntrega", fechaHoraEntrega);
            spEstado.setParameter("idEmpleadoEntrega", idEmpleadoEntrega);
            spEstado.execute();

            return null;
        });
    }

    @Override
    public void clearContext() {
        var em = TransactionManager.getEntityManager();
        if (em != null) {
            em.flush();
            em.clear();
        }
    }

}

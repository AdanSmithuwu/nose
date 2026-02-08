package com.comercialvalerio.infrastructure.persistence.impl;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.domain.model.Venta;
import com.comercialvalerio.domain.repository.VentaRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.VentaEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.VentaMapper;
import com.comercialvalerio.infrastructure.persistence.tvp.SqlServerTvpBuilder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.StoredProcedureQuery;

@ApplicationScoped
public class VentaRepositoryImpl extends BaseRepository implements VentaRepository {
@Inject
    VentaMapper mapper;

    /* ---------- LECTURA ---------- */
    @Override
    public List<Venta> findAll() {
        return read(em -> map(
                em.createNamedQuery("Venta.findAll", VentaEntity.class),
                mapper::toDomain));
    }

    @Override
    public java.util.Optional<Venta> findById(Integer id) {
        return readOptional(em -> em.createNamedQuery("Venta.findByIdTransaccion", VentaEntity.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .map(mapper::toDomain)
                .orElse(null));
    }

    @Override
    public List<Venta> findByRangoFecha(LocalDateTime d, LocalDateTime h) {
        return read(em -> map(
                em.createNamedQuery("Venta.findByRangoFecha", VentaEntity.class)
                        .setParameter("d", d)
                        .setParameter("h", h),
                mapper::toDomain));
    }

    @Override
    public List<Venta> findByCliente(Integer idCliente) {
        return read(em -> map(
                em.createNamedQuery("Venta.findByCliente", VentaEntity.class)
                        .setParameter("idCli", idCliente),
                mapper::toDomain));
    }

    /* ---------- ESCRITURA ---------- */
    @Override
    public void save(Venta v) {
        tx(em -> {
            StoredProcedureQuery sp = em.createNamedStoredProcedureQuery("Venta.registrar");
            sp.setParameter("idEmpleado", v.getEmpleado().getIdPersona());
            sp.setParameter("idCliente", v.getCliente() == null ? null : v.getCliente().getIdPersona());
            sp.setParameter("observacion", v.getObservacion());
            try {
                sp.setParameter("detalle", SqlServerTvpBuilder.detalle(v.getDetalles()));
                sp.setParameter("pagos", SqlServerTvpBuilder.pagos(v.getPagos()));
                sp.execute();
            } catch (java.sql.SQLException ex) {
                throw translate(ex);
            }
            v.setIdTransaccion((Integer) sp.getOutputParameterValue("idTransaccion"));
            return null;
        });
    }

    @Override
    public void cancelar(Integer idVenta, String motivo) {
        tx(em -> {
            StoredProcedureQuery sp = em.createNamedStoredProcedureQuery("Venta.cancelar");
            sp.setParameter("idTransaccion", idVenta);
            sp.setParameter("motivoCancelacion", motivo);
            sp.execute();
            return null;
        });
    }

    @Override
    public boolean existsById(Integer idTx) {
        /*  Cuenta la fila;  >0  ⇒ existe.  */
        return read(em -> em.createNamedQuery("Venta.countById", Long.class)
                .setParameter("id", idTx)
                .getSingleResult() > 0);
    }
}

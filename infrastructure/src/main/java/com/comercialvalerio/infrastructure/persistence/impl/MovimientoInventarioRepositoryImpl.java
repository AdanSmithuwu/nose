package com.comercialvalerio.infrastructure.persistence.impl;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.domain.model.MovimientoInventario;
import com.comercialvalerio.domain.repository.MovimientoInventarioRepository;
import com.comercialvalerio.infrastructure.persistence.CrudRepository;
import com.comercialvalerio.infrastructure.persistence.entity.MovimientoInventarioEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.MovimientoInventarioMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MovimientoInventarioRepositoryImpl
        extends CrudRepository<MovimientoInventario, MovimientoInventarioEntity, Integer>
        implements MovimientoInventarioRepository {
    @Inject
    MovimientoInventarioMapper mapper;

    public MovimientoInventarioRepositoryImpl() {
        super(MovimientoInventarioEntity.class);
    }

    /* ---------- Lectura ---------- */
    @Override
    public List<MovimientoInventario> findByProducto(Integer idProducto) {
        return read(em -> map(
                em.createNamedQuery("MovimientoInventario.findByProducto",
                                     MovimientoInventarioEntity.class)
                        .setParameter("idProd", idProducto),
                mapper::toDomain));
    }

    @Override
    public boolean existsByProducto(Integer idProducto) {
        return read(em -> em.createNamedQuery(
                        "MovimientoInventario.countByProducto",
                        Long.class)
                .setParameter("idProd", idProducto)
                .getSingleResult() > 0);
    }

    @Override
    public boolean existsByProductoAndMotivoNot(Integer idProducto, String motivo) {
        return read(em -> em.createNamedQuery(
                        "MovimientoInventario.countByProductoAndMotivoNot",
                        Long.class)
                .setParameter("idProd", idProducto)
                .setParameter("motivo", motivo)
                .getSingleResult() > 0);
    }

    @Override
    public boolean existsByTallaStockAndMotivoNot(Integer idTallaStock, String motivo) {
        return read(em -> em.createNamedQuery(
                        "MovimientoInventario.countByTallaStockAndMotivoNot",
                        Long.class)
                .setParameter("idTall", idTallaStock)
                .setParameter("motivo", motivo)
                .getSingleResult() > 0);
    }

    @Override
    public boolean existsMovimientosByEmpleado(Integer idEmpleado) {
        return read(em -> em.createNamedQuery(
                        "MovimientoInventario.countByEmpleado",
                        Long.class)
                .setParameter("idEmp", idEmpleado)
                .getSingleResult() > 0);
    }

    @Override
    public List<MovimientoInventario> findByRangoFecha(LocalDateTime desde,
                                                       LocalDateTime hasta) {
        return read(em -> map(
                em.createNamedQuery("MovimientoInventario.findByRangoFecha",
                                     MovimientoInventarioEntity.class)
                        .setParameter("desde", desde)
                        .setParameter("hasta", hasta),
                mapper::toDomain));
    }

    /* ---------- Escritura ---------- */
    @Override
    protected MovimientoInventarioEntity toEntity(MovimientoInventario model) {
        return mapper.toEntity(model);
    }

    @Override
    protected MovimientoInventario toDomain(MovimientoInventarioEntity entity) {
        return mapper.toDomain(entity);
    }

    @Override
    protected void assignId(MovimientoInventario model, MovimientoInventarioEntity entity) {
        model.setIdMovimiento(entity.getIdMovimiento());
    }
}

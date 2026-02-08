package com.comercialvalerio.infrastructure.persistence.impl;
import java.math.BigDecimal;
import java.util.List;

import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.model.TallaStock;
import com.comercialvalerio.domain.repository.TallaStockRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.TallaStockEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.TallaStockMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.StoredProcedureQuery;

@ApplicationScoped
public class TallaStockRepositoryImpl extends BaseRepository
                                      implements TallaStockRepository {
@Inject
    TallaStockMapper mapper;
    /* -------- Lectura -------- */
    @Override
    public List<TallaStock> findByProducto(Integer idProducto) {
        return read(em -> map(
                em.createNamedQuery("TallaStock.findByProducto", TallaStockEntity.class)
                        .setParameter("idProd", idProducto),
                mapper::toDomain));
    }

    @Override
    public List<TallaStock> findAllById(java.util.Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return read(em -> map(
                em.createNamedQuery("TallaStock.findByIds", TallaStockEntity.class)
                        .setParameter("ids", ids),
                mapper::toDomain));
    }

    @Override
    public List<TallaStock> findByProductos(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return read(em -> map(
                em.createNamedQuery("TallaStock.findByProductos", TallaStockEntity.class)
                        .setParameter("ids", ids),
                mapper::toDomain));
    }
    @Override
    public java.util.Optional<TallaStock> findByProductoAndTalla(Integer idProducto, String talla) {
        return readOptional(em -> em.createNamedQuery("TallaStock.findByProductoAndTalla", TallaStockEntity.class)
                            .setParameter("idProd", idProducto)
                            .setParameter("talla", talla)
                            .getResultStream()
                            .findFirst()
                            .map(mapper::toDomain)
                            .orElse(null));
    }
    @Override
    public java.util.Optional<TallaStock> findById(Integer id) {
        return readOptional(em -> {
            TallaStockEntity e = em.find(TallaStockEntity.class, id);
            return e == null ? null : mapper.toDomain(e);
        });
    }
    /* -------- Escritura -------- */
    @Override
    public void save(TallaStock ts) {
        tx(em -> {
            /* merge e ID */
            TallaStockEntity ent = mapper.toEntity(ts);
            ent.setProducto(em.getReference(
                    com.comercialvalerio.infrastructure.persistence.entity.ProductoEntity.class,
                    ts.getProducto().getIdProducto()));
            ent = em.merge(ent);
            ts.setIdTallaStock(ent.getIdTallaStock());
            return null;
        });
    }
    @Override
    public void delete(Integer id) {
        tx(em -> {
            TallaStockEntity e = em.find(TallaStockEntity.class, id);
            if (e == null)
                throw new EntityNotFoundException("TallaStock no encontrado (id=" + id + ")");
            em.remove(e);
            return null;
        });
    }
    @Override
    public void ajustarStock(Integer idTallaStock, BigDecimal delta) {
        tx(em -> {
            TallaStockEntity talla = em.find(TallaStockEntity.class, idTallaStock);
            if (talla == null)
                throw new EntityNotFoundException("TallaStock no encontrado (id=" + idTallaStock + ")");
            BigDecimal nuevo = talla.getStock().add(delta);
            if (nuevo.compareTo(BigDecimal.ZERO) < 0)
                throw new BusinessRuleViolationException("Stock negativo no permitido");
            talla.setStock(nuevo);
            return null;
        });
    }

    @Override
    public void updateEstado(Integer idTallaStock, String estado) {
        tx(em -> {
            var nuevo = em.createNamedQuery("Estado.findByModuloAndNombre",
                    com.comercialvalerio.infrastructure.persistence.entity.EstadoEntity.class)
                    .setParameter("m", "Producto")
                    .setParameter("n", estado)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (nuevo == null)
                throw new EntityNotFoundException("Estado no encontrado");

            int n = em.createNamedQuery("TallaStock.updateEstado")
                    .setParameter("e", nuevo)
                    .setParameter("id", idTallaStock)
                    .executeUpdate();
            if (n == 0)
                throw new EntityNotFoundException("TallaStock no encontrado");
            return null;
        });
    }

    @Override
    public void updateEstadoByProducto(Integer idProducto, String estado) {
        tx(em -> {
            var nuevo = em.createNamedQuery("Estado.findByModuloAndNombre",
                    com.comercialvalerio.infrastructure.persistence.entity.EstadoEntity.class)
                    .setParameter("m", "Producto")
                    .setParameter("n", estado)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (nuevo == null)
                throw new EntityNotFoundException("Estado no encontrado");

            em.createNamedQuery("TallaStock.updateEstadoByProducto")
               .setParameter("e", nuevo)
               .setParameter("p", idProducto)
               .executeUpdate();
            return null;
        });
    }

    @Override
    public void recalcularStocks() {
        tx(em -> {
            StoredProcedureQuery sp = em.createNamedStoredProcedureQuery("Producto.recalcularStock");
            sp.execute();
            return null;
        });
    }
}

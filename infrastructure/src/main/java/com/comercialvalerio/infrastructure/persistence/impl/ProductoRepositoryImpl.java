package com.comercialvalerio.infrastructure.persistence.impl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.model.Producto;
import com.comercialvalerio.domain.model.TipoPedido;
import com.comercialvalerio.domain.repository.ProductoRepository;
import com.comercialvalerio.infrastructure.persistence.CrudRepository;
import com.comercialvalerio.infrastructure.persistence.entity.ProductoEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.ProductoMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.StoredProcedureQuery;

@ApplicationScoped
public class ProductoRepositoryImpl
        extends CrudRepository<Producto, ProductoEntity, Integer>
        implements ProductoRepository {
@Inject
    ProductoMapper mapper;

    public ProductoRepositoryImpl() {
        super(ProductoEntity.class);
    }
    @Override
    public List<Producto> findAllById(java.util.Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return read(em -> map(
                em.createNamedQuery("Producto.findByIds", ProductoEntity.class)
                        .setParameter("ids", ids),
                mapper::toDomain));
    }
    /* ---------- Lectura ---------- */
    @Override
    public List<Producto> findByNombreLike(String patron) {
        return read(em -> map(
                em.createNamedQuery("Producto.findByNombreLike", ProductoEntity.class)
                        .setParameter("patron", "%" + patron.toUpperCase(Locale.ROOT) + "%"),
                mapper::toDomain));
    }
    @Override
    public List<Producto> findByCategoria(Integer idCategoria) {
        return read(em -> map(
                em.createNamedQuery("Producto.findByCategoria", ProductoEntity.class)
                        .setParameter("idCat", idCategoria),
                mapper::toDomain));
    }
    @Override
    public List<Producto> findBajoStock() {
        return read(em -> map(
                em.createNamedQuery("Producto.bajoStock", ProductoEntity.class),
                mapper::toDomain));
    }

    @Override
    public boolean existsByNombre(String nombre, Integer excludeId) {
        return read(em -> em.createNamedQuery("Producto.existsByNombre", Long.class)
                .setParameter("n", nombre.toUpperCase(Locale.ROOT))
                .setParameter("id", excludeId)
                .getSingleResult() > 0);
    }

    @Override
    public boolean existsByCategoria(Integer idCategoria) {
        return read(em -> em.createNamedQuery("Producto.existsByCategoria", Long.class)
                .setParameter("id", idCategoria)
                .getSingleResult() > 0);
    }

    @Override
    public List<Producto> findByFiltros(String nombre, Integer idCategoria,
                                        Integer idTipoProducto,
                                        String talla, String unidad) {
        return read(em -> map(
                em.createNamedQuery("Producto.byFiltros", ProductoEntity.class)
                        .setParameter("nombre", nombre)
                        .setParameter("idCat", idCategoria)
                        .setParameter("idTipo", idTipoProducto)
                        .setParameter("talla", talla)
                        .setParameter("unidad", unidad),
                mapper::toDomain));
    }

    @Override
    public List<Producto> findWithTallasAndPresentaciones() {
        return read(em -> map(
                em.createNamedQuery("Producto.withTallasAndPresentaciones", ProductoEntity.class),
                mapper::toDomain));
    }

    @Override
    public List<Producto> findParaPedido(String nombre, TipoPedido tipoPedidoDefault) {
        return read(em -> map(
                em.createNamedQuery("Producto.paraPedido", ProductoEntity.class)
                        .setParameter("nombre", nombre)
                        .setParameter("tipo", tipoPedidoDefault == null
                                ? null : tipoPedidoDefault.getNombre()),
                mapper::toDomain));
    }
    /* ---------- Escritura ---------- */
    @Override
    public void save(Producto p) {
        tx(em -> {
            ProductoEntity ent = mapper.toEntity(p);
            boolean nuevo = ent.getIdProducto() == null;
            ent = em.merge(ent);             // insertar / actualizar
            if (nuevo) {
                em.flush();                // asegura generación de ID
            }
            p.setIdProducto(ent.getIdProducto());
            return null;
        });
    }
    @Override
    public void delete(Integer id) {
        tx(em -> {
            ProductoEntity e = em.find(ProductoEntity.class, id);
            if (e == null)
                throw new EntityNotFoundException("Producto no encontrado (id=" + id + ")");

            em.remove(e);
            return null;
        });
    }
    @Override
    public void actualizarStock(Integer idProducto, BigDecimal nuevoStock) {
        tx(em -> {
            ProductoEntity e = em.find(ProductoEntity.class, idProducto);
            if (e == null)
                throw new EntityNotFoundException("Producto no encontrado (id=" + idProducto + ")");
            e.setStockActual(nuevoStock);
            return null;
        });
    }

    @Override
    public void updateEstado(Integer idProducto, String estado) {
        tx(em -> {
            var nuevo = em.createNamedQuery("Estado.findByModuloAndNombre", com.comercialvalerio.infrastructure.persistence.entity.EstadoEntity.class)
                    .setParameter("m", "Producto")
                    .setParameter("n", estado)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (nuevo == null)
                throw new EntityNotFoundException("Estado no encontrado");

            ProductoEntity prod = em.find(ProductoEntity.class, idProducto);
            if (prod == null)
                throw new EntityNotFoundException("Producto no encontrado");

            prod.setEstado(nuevo);
            em.merge(prod);
            return null;
        });
    }

    @Override
    public void setIgnorarUmbralHastaCero(Integer idProducto, boolean ignorar) {
        tx(em -> {
            ProductoEntity prod = em.find(ProductoEntity.class, idProducto);
            if (prod == null)
                throw new EntityNotFoundException("Producto no encontrado");
            prod.setIgnorarUmbralHastaCero(ignorar);
            em.merge(prod);
            return null;
        });
    }

    @Override
    public void actualizarMinMayoristaHilo(int nuevoMin) {
        tx(em -> {
            em.createQuery("UPDATE Producto p SET p.minMayorista = :m "
                            + "WHERE UPPER(p.nombre) LIKE 'OVILLO DE HILO%' "
                            + "AND p.mayorista = true")
                    .setParameter("m", nuevoMin)
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

    @Override
    protected ProductoEntity toEntity(Producto model) {
        return mapper.toEntity(model);
    }

    @Override
    protected Producto toDomain(ProductoEntity entity) {
        return mapper.toDomain(entity);
    }

    @Override
    protected void assignId(Producto model, ProductoEntity entity) {
        model.setIdProducto(entity.getIdProducto());
    }
}

package com.comercialvalerio.infrastructure.service;

import java.math.BigDecimal;

import com.comercialvalerio.domain.model.MovimientoInventario;
import com.comercialvalerio.domain.model.Producto;
import com.comercialvalerio.domain.model.TallaStock;
import com.comercialvalerio.domain.repository.MovimientoInventarioRepository;
import com.comercialvalerio.domain.repository.ProductoRepository;
import com.comercialvalerio.domain.repository.TallaStockRepository;
import com.comercialvalerio.infrastructure.transaction.TransactionManager;

import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import com.comercialvalerio.domain.service.InventarioService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Implementación de {@link InventarioService}. */
@ApplicationScoped
public class InventarioServiceImpl implements InventarioService {

    private final MovimientoInventarioRepository repoMov;
    private final ProductoRepository             repoProd;
    private final TallaStockRepository           repoTall;

    @Inject
    public InventarioServiceImpl(MovimientoInventarioRepository repoMov,
                                 ProductoRepository             repoProd,
                                 TallaStockRepository           repoTall) {
        this.repoMov  = repoMov;
        this.repoProd = repoProd;
        this.repoTall = repoTall;
    }

    @Override
    public MovimientoInventario registrarMovimiento(MovimientoInventario mov) {
        repoMov.save(mov);
        return mov;
    }

    @Override
    public boolean tieneStock(Producto producto, TallaStock tallaStock,
                              BigDecimal cantidad) {
        if (producto == null || cantidad == null) return false;

        BigDecimal stockProd = TransactionManager.runWithSession(em ->
                em.createNamedQuery("Producto.stockActualById", BigDecimal.class)
                  .setParameter("id", producto.getIdProducto())
                  .setHint("jakarta.persistence.cache.retrieveMode",
                           CacheRetrieveMode.BYPASS)
                  .setHint("jakarta.persistence.cache.storeMode",
                           CacheStoreMode.BYPASS)
                  .getResultStream()
                  .findFirst()
                  .orElse(null));

        if (stockProd == null || stockProd.compareTo(cantidad) < 0) {
            return false;
        }

        if (tallaStock != null) {
            BigDecimal stockTalla = TransactionManager.runWithSession(em ->
                    em.createNamedQuery("TallaStock.stockById", BigDecimal.class)
                      .setParameter("id", tallaStock.getIdTallaStock())
                      .setHint("jakarta.persistence.cache.retrieveMode",
                               CacheRetrieveMode.BYPASS)
                      .setHint("jakarta.persistence.cache.storeMode",
                               CacheStoreMode.BYPASS)
                      .getResultStream()
                      .findFirst()
                      .orElse(null));
            if (stockTalla == null || stockTalla.compareTo(cantidad) < 0) {
                return false;
            }
        }

        return true;
    }
}

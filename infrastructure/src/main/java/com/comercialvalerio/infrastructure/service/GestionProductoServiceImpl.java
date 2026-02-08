package com.comercialvalerio.infrastructure.service;

import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.model.Producto;
import com.comercialvalerio.common.DependencyUtils;
import com.comercialvalerio.domain.repository.DetalleTransaccionRepository;
import com.comercialvalerio.domain.repository.MovimientoInventarioRepository;
import com.comercialvalerio.domain.repository.OrdenCompraRepository;
import com.comercialvalerio.domain.repository.ProductoRepository;
import com.comercialvalerio.domain.repository.AlertaStockRepository;
import com.comercialvalerio.domain.security.AdminChecks;
import com.comercialvalerio.domain.service.GestionProductoService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Implementación de {@link GestionProductoService}. */
@ApplicationScoped
public class GestionProductoServiceImpl implements GestionProductoService {

    private final ProductoRepository repoProd;
    private final MovimientoInventarioRepository repoMov;
    private final DetalleTransaccionRepository repoDet;
    private final OrdenCompraRepository repoOrden;
    private final AlertaStockRepository repoAlerta;

    @Inject
    public GestionProductoServiceImpl(ProductoRepository repoProd,
                                      MovimientoInventarioRepository repoMov,
                                      DetalleTransaccionRepository repoDet,
                                      OrdenCompraRepository repoOrden,
                                      AlertaStockRepository repoAlerta) {
        this.repoProd = repoProd;
        this.repoMov = repoMov;
        this.repoDet = repoDet;
        this.repoOrden = repoOrden;
        this.repoAlerta = repoAlerta;
    }

    @Override
    public Producto guardarProducto(Producto producto) {
        boolean duplicado = repoProd.existsByNombre(producto.getNombre(),
                                                   producto.getIdProducto());
        if (duplicado) {
            throw new DuplicateEntityException(
                    "Ya existe un producto llamado «" + producto.getNombre() + "»");
        }
        repoProd.save(producto);
        return producto;
    }

    @Override
    public void eliminarProducto(Integer idProducto) {
        AdminChecks.requireAdminRole();
        Producto prod = repoProd.findById(idProducto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Producto no encontrado (id=" + idProducto + ")"));
        if (repoMov.existsByProductoAndMotivoNot(idProducto, "Stock inicial")) {
            throw new BusinessRuleViolationException(
                    "No se puede eliminar: existen movimientos de inventario para este producto");
        }
        if (repoDet.existsByProducto(idProducto)) {
            throw new BusinessRuleViolationException(
                    "No se puede eliminar: existen detalles de transacción para este producto");
        }
        if (repoOrden.existsByProducto(idProducto)) {
            throw new BusinessRuleViolationException(
                    "No se puede eliminar: existen órdenes de compra para este producto");
        }
        repoAlerta.deleteByProducto(idProducto);
        repoProd.delete(idProducto);
    }

    @Override
    public void recalcularStockGlobal() {
        repoProd.recalcularStocks();
    }

    @Override
    public java.util.List<String> findDependencias(Integer idProducto) {
        java.util.List<String> deps = new java.util.ArrayList<>();
        DependencyUtils.addIf(repoMov.existsByProductoAndMotivoNot(idProducto, "Stock inicial"),
                              "movimientos de inventario", deps);
        DependencyUtils.addIf(repoDet.existsByProducto(idProducto), "detalles de transacción", deps);
        DependencyUtils.addIf(repoOrden.existsByProducto(idProducto), "órdenes de compra", deps);
        return deps;
    }
}

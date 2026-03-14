package com.comercialvalerio.application.service.impl;

import java.math.BigDecimal;

import com.comercialvalerio.application.dto.MovimientoInventarioCreateDto;
import com.comercialvalerio.application.dto.ProductoCUDto;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.model.Producto;
import com.comercialvalerio.domain.model.TallaStock;
import com.comercialvalerio.domain.model.TipoMovimiento;
import com.comercialvalerio.domain.model.TipoProducto;
import com.comercialvalerio.domain.model.TipoProductoNombre;
import com.comercialvalerio.domain.repository.PresentacionRepository;
import com.comercialvalerio.domain.repository.TallaStockRepository;
import com.comercialvalerio.domain.repository.TipoMovimientoRepository;
import com.comercialvalerio.domain.repository.MovimientoInventarioRepository;
import com.comercialvalerio.domain.security.RequestContext;
import com.comercialvalerio.application.service.MovimientoInventarioService;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.cache.EstadoCache;

/** Ayudante con métodos de utilidad para operaciones CRUD de Producto. */
public final class ProductoCrudHelper {
    private ProductoCrudHelper() {}

    private enum SubState { NONE, TALLAS, PRESENTACIONES }

    private static SubState checkSubcollections(ProductoCUDto in) {
        boolean hasTallas = in.tallas() != null && !in.tallas().isEmpty();
        boolean hasPres   = in.presentaciones() != null && !in.presentaciones().isEmpty();
        if (hasTallas && hasPres) {
            throw new BusinessRuleViolationException(
                    "Solo puede registrar tallas o presentaciones, no ambos");
        }
        if (hasTallas) return SubState.TALLAS;
        if (hasPres) return SubState.PRESENTACIONES;
        return SubState.NONE;
    }

    public static BigDecimal deriveStock(ProductoCUDto in, TipoProducto tipo) {
        return in.stockActual();
    }

    public static void syncSubcollections(Producto prod, ProductoCUDto in,
                                          TipoProducto tipo, boolean nuevoProducto,
                                          TallaStockRepository repoTalla,
                                          PresentacionRepository repoPres,
                                          MovimientoInventarioRepository repoMov,
                                          EstadoCache estadoCache) {
        SubState state = checkSubcollections(in);
        TipoProductoNombre nombre = TipoProductoNombre.fromNombre(
                tipo.getNombre());
        switch (nombre) {
            case VESTIMENTA -> syncVestimenta(prod, state, in, repoTalla, repoMov, estadoCache, nuevoProducto);
            case FRACCIONABLE -> syncFraccionable(prod, state, in, repoPres, estadoCache);
            default -> syncUnidadFija(prod, state, repoTalla, repoPres);
        }
    }

    private static void syncVestimenta(Producto prod, SubState state,
                                       ProductoCUDto in, TallaStockRepository repoTalla,
                                       MovimientoInventarioRepository repoMov,
                                       EstadoCache estadoCache,
                                       boolean nuevoProducto) {
        if (state == SubState.PRESENTACIONES) {
            throw new BusinessRuleViolationException(
                    "Vestimenta no admite presentaciones");
        }
        ProductoSubcollectionHelper.sincronizarTallas(prod, in.tallas(),
                repoTalla, repoMov, estadoCache, nuevoProducto);
    }

    private static void syncFraccionable(Producto prod, SubState state,
                                         ProductoCUDto in, PresentacionRepository repoPres,
                                         EstadoCache estadoCache) {
        if (state == SubState.TALLAS) {
            throw new BusinessRuleViolationException(
                    "Fraccionable no admite tallas");
        }
        ProductoSubcollectionHelper.sincronizarPresentaciones(prod, in.presentaciones(), repoPres, estadoCache);
    }

    private static void syncUnidadFija(Producto prod, SubState state,
                                       TallaStockRepository repoTalla,
                                       PresentacionRepository repoPres) {
        if (state != SubState.NONE) {
            throw new BusinessRuleViolationException(
                    "Unidad fija no admite tallas ni presentaciones");
        }
        repoTalla.findByProducto(prod.getIdProducto())
                 .forEach(ts -> repoTalla.delete(ts.getIdTallaStock()));
        repoPres.findByProducto(prod.getIdProducto())
                .forEach(pr -> repoPres.delete(pr.getIdPresentacion()));
    }

    public static void registrarMovimientosIniciales(Producto prod,
                                                     TipoProducto tipo,
                                                     BigDecimal stock,
                                                     java.util.List<com.comercialvalerio.application.dto.TallaStockCUDto> tallas,
                                                     MovimientoInventarioService movSvc,
                                                     TallaStockRepository repoTalla,
                                                     TipoMovimientoRepository repoTipoMov) {
        TipoMovimiento tmEntrada = ServiceChecks.requireFound(
                repoTipoMov.findByNombre("Entrada"),
                "TipoMovimiento Entrada no encontrado");
        Integer idEmp = RequestContext.idEmpleado();
        if (idEmp == null) {
            throw new BusinessRuleViolationException("idEmpleado obligatorio");
        }
        TipoProductoNombre nombre = TipoProductoNombre.fromNombre(
                tipo.getNombre());
        switch (nombre) {
            case UNIDAD_FIJA -> {
                if (stock != null) {
                    movSvc.registrar(new MovimientoInventarioCreateDto(
                            prod.getIdProducto(),
                            null,
                            tmEntrada.getIdTipoMovimiento(),
                            stock,
                            "Stock inicial",
                            idEmp));
                }
            }
            case VESTIMENTA -> {
                java.util.Map<String, java.math.BigDecimal> map = new java.util.HashMap<>();
                if (tallas != null) {
                    for (var dto : tallas) {
                        if (dto.stock() != null && dto.stock().compareTo(java.math.BigDecimal.ZERO) > 0) {
                            map.put(dto.talla().toUpperCase(java.util.Locale.ROOT), dto.stock());
                        }
                    }
                }
                for (TallaStock ts : repoTalla.findByProducto(prod.getIdProducto())) {
                    java.math.BigDecimal s = map.get(ts.getTalla().toUpperCase(java.util.Locale.ROOT));
                    if (s != null) {
                        movSvc.registrar(new MovimientoInventarioCreateDto(
                                prod.getIdProducto(),
                                ts.getIdTallaStock(),
                                tmEntrada.getIdTipoMovimiento(),
                                s,
                                "Stock inicial",
                                idEmp));
                    }
                }
            }
            case FRACCIONABLE -> {
                if (stock != null) {
                    movSvc.registrar(new MovimientoInventarioCreateDto(
                            prod.getIdProducto(),
                            null,
                            tmEntrada.getIdTipoMovimiento(),
                            stock,
                            "Stock inicial",
                            idEmp));
                }
            }
            default -> {
                // No se registran movimientos para otros tipos
            }
        }
    }
}

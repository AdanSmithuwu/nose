package com.comercialvalerio.application.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.comercialvalerio.application.dto.PresentacionCUDto;
import com.comercialvalerio.application.dto.TallaStockCUDto;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.model.Presentacion;
import com.comercialvalerio.domain.model.Producto;
import com.comercialvalerio.domain.model.TallaStock;
import com.comercialvalerio.domain.model.Estado;
import com.comercialvalerio.domain.model.EstadoNombre;
import com.comercialvalerio.application.cache.EstadoCache;
import com.comercialvalerio.domain.repository.PresentacionRepository;
import com.comercialvalerio.domain.repository.TallaStockRepository;
import com.comercialvalerio.domain.repository.MovimientoInventarioRepository;
import com.comercialvalerio.application.mapper.SubcollectionMapper;

/** Ayudante para sincronizar las subcolecciones de tallas y presentaciones. */
public final class ProductoSubcollectionHelper {
    private ProductoSubcollectionHelper() {}

    public static void sincronizarTallas(Producto prod, List<TallaStockCUDto> dtoList,
                                         TallaStockRepository repoTalla,
                                         MovimientoInventarioRepository repoMov,
                                         EstadoCache estadoCache,
                                         boolean nuevoProducto) {
        if (dtoList == null || dtoList.isEmpty()) {
            throw new BusinessRuleViolationException("Vestimenta requiere al menos una talla");
        }

        java.util.Set<String> tallas = new java.util.HashSet<>();
        for (TallaStockCUDto dto : dtoList) {
            if (!tallas.add(dto.talla().toUpperCase())) {
                throw new DuplicateEntityException(
                        "Ya existe esa talla para el producto");
            }
        }

        Map<Integer, TallaStock> actuales = repoTalla.findByProducto(prod.getIdProducto())
                .stream().collect(Collectors.toMap(TallaStock::getIdTallaStock, x -> x));

        dtoList.forEach(in -> {
            TallaStock ts = in.idTallaStock() == null
                    ? new TallaStock()
                    : actuales.remove(in.idTallaStock());
            if (ts == null) {
                throw new EntityNotFoundException("TallaStock id=" + in.idTallaStock() + " no existe");
            }
            SubcollectionMapper.apply(in, prod, ts, nuevoProducto);
            if (ts.getEstado() == null) {
                Estado activo = estadoCache.get("Producto", EstadoNombre.ACTIVO);
                ts.setEstado(activo);
            }
            repoTalla.save(ts);
        });

        if (actuales.values().removeIf(ts ->
                ts.getStock().compareTo(BigDecimal.ZERO) > 0
                        && repoMov.existsByTallaStockAndMotivoNot(
                                ts.getIdTallaStock(), "Stock inicial"))) {
            throw new BusinessRuleViolationException(
                    "No se puede eliminar talla con stock");
        }
        actuales.values().forEach(t -> repoTalla.delete(t.getIdTallaStock()));
    }

    public static void sincronizarPresentaciones(Producto prod, List<PresentacionCUDto> dtoList,
                                                 PresentacionRepository repoPres,
                                                 EstadoCache estadoCache) {
        if (dtoList == null || dtoList.isEmpty()) {
            throw new BusinessRuleViolationException("Fraccionable requiere al menos una presentación");
        }

        java.util.Set<java.math.BigDecimal> cantidades = new java.util.HashSet<>();
        for (PresentacionCUDto dto : dtoList) {
            if (!cantidades.add(dto.cantidad())) {
                throw new DuplicateEntityException(
                        "Ya existe una presentación con esa cantidad para el producto");
            }
        }

        Map<Integer, Presentacion> actuales = repoPres.findByProducto(prod.getIdProducto())
                .stream().collect(Collectors.toMap(Presentacion::getIdPresentacion, x -> x));

        java.util.Set<Integer> ids = dtoList.stream()
                .map(PresentacionCUDto::idPresentacion)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        actuales.entrySet().removeIf(e -> {
            if (!ids.contains(e.getKey())) {
                repoPres.delete(e.getKey());
                return true;
            }
            return false;
        });

        dtoList.forEach(in -> {
            Presentacion pr = in.idPresentacion() == null
                    ? new Presentacion()
                    : actuales.get(in.idPresentacion());
            if (pr == null) {
                throw new EntityNotFoundException("Presentación id=" + in.idPresentacion() + " no existe");
            }
            SubcollectionMapper.apply(in, prod, pr);
            if (pr.getEstado() == null) {
                Estado activo = estadoCache.get("Producto", EstadoNombre.ACTIVO);
                pr.setEstado(activo);
            }
            repoPres.save(pr);
        });
    }
}

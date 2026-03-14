package com.comercialvalerio.application.service.impl;
import java.math.BigDecimal;
import java.util.List;

import com.comercialvalerio.application.cache.EstadoCache;
import com.comercialvalerio.application.dto.TallaStockCreateDto;
import com.comercialvalerio.application.dto.TallaStockDto;
import com.comercialvalerio.application.mapper.SubcollectionMapper;
import com.comercialvalerio.application.mapper.TallaStockDtoMapper;
import com.comercialvalerio.application.service.TallaStockService;
import com.comercialvalerio.application.service.util.SecurityChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.DependencyUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.model.Estado;
import com.comercialvalerio.domain.model.EstadoNombre;
import com.comercialvalerio.domain.model.Producto;
import com.comercialvalerio.domain.model.TallaStock;
import com.comercialvalerio.domain.repository.MovimientoInventarioRepository;
import com.comercialvalerio.domain.repository.ProductoRepository;
import com.comercialvalerio.domain.repository.TallaStockRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class TallaStockServiceImpl implements TallaStockService {

    private final TallaStockRepository repoTall;
    private final ProductoRepository   repoProd;
    private final MovimientoInventarioRepository repoMov;
    private final EstadoCache          estadoCache;
@Inject
    TallaStockDtoMapper mapper;

    @Inject
    public TallaStockServiceImpl(TallaStockRepository repoTall,
                                 ProductoRepository   repoProd,
                                 MovimientoInventarioRepository repoMov,
                                 EstadoCache          estadoCache) {
        this.repoTall    = repoTall;
        this.repoProd    = repoProd;
        this.repoMov    = repoMov;
        this.estadoCache = estadoCache;
    }

    @Override public List<TallaStockDto> listarPorProducto(Integer idProd) {
        return ServiceUtils.mapList(
                repoTall.findByProducto(idProd).stream()
                       .filter(ts -> EstadoNombre.ACTIVO.equalsNombre(
                               ts.getEstado().getNombre()))
                       .toList(),
                mapper::toDto);
    }

    @Override
    public TallaStockDto obtener(Integer id) {
        TallaStock ts = repoTall.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TallaStock no encontrado"));
        return mapper.toDto(ts);
    }

    @Override public TallaStockDto crear(TallaStockCreateDto in) {
        Producto prod = repoProd.findById(in.idProducto())
                .orElseThrow(() -> new EntityNotFoundException("Producto inexistente"));

        if (repoTall.findByProductoAndTalla(in.idProducto(), in.talla()).isPresent())
            throw new DuplicateEntityException("Ya existe esa talla para el producto");

        TallaStock ts = SubcollectionMapper.fromDto(in, prod);
        if (ts.getEstado() == null) {
            Estado activo = estadoCache.get("Producto", EstadoNombre.ACTIVO);
            ts.setEstado(activo);
        }
        repoTall.save(ts);
        return mapper.toDto(ts);
    }

    @Override public TallaStockDto actualizar(Integer id, TallaStockCreateDto in) {
        TallaStock ts = repoTall.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TallaStock no encontrado"));

        Producto prod = repoProd.findById(in.idProducto())
                .orElseThrow(() -> new EntityNotFoundException("Producto inexistente"));

        /* Verifica unicidad si cambia talla o producto */
        java.util.Optional<TallaStock> duplicado =
            repoTall.findByProductoAndTalla(in.idProducto(),in.talla());
        if (duplicado.isPresent() && !duplicado.get().getIdTallaStock().equals(id))
            throw new DuplicateEntityException("Ya existe esa talla para el producto");

        SubcollectionMapper.updateFromDto(in, prod, ts);
        repoTall.save(ts);
        return mapper.toDto(ts);
    }

    @Override
    public void eliminar(Integer id) {
        SecurityChecks.requireAdminRole();
        repoTall.delete(id);
    }

    @Override public void ajustarStock(Integer idTallaStock, BigDecimal delta) {
        repoTall.ajustarStock(idTallaStock, delta);
    }

    @Override public void activar(Integer id) {
        repoTall.updateEstado(id, EstadoNombre.ACTIVO.getNombre());
    }

    @Override public void desactivar(Integer id) {
        repoTall.updateEstado(id, EstadoNombre.INACTIVO.getNombre());
    }

    @Override
    public java.util.List<String> obtenerDependencias(Integer idTallaStock) {
        java.util.List<String> deps = new java.util.ArrayList<>();
        DependencyUtils.addIf(
                repoMov.existsByTallaStockAndMotivoNot(idTallaStock, "Stock inicial"),
                "movimientos de inventario", deps);
        return deps;
    }

    @Override
    public List<TallaStockDto> listarTodosPorProducto(Integer idProd) {
        return ServiceUtils.mapList(repoTall.findByProducto(idProd), mapper::toDto);
    }

    @Override
    public void recalcularStockGlobal() {
        repoTall.recalcularStocks();
    }
}

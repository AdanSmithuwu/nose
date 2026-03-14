package com.comercialvalerio.application.service.impl;
import java.math.BigDecimal;
import java.util.List;

import com.comercialvalerio.application.cache.EstadoCache;
import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.dto.ProductoCUDto;
import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.application.dto.ProductoMasVendidoDto;
import com.comercialvalerio.application.dto.ProductoVentaDto;
import com.comercialvalerio.application.dto.TallaStockCUDto;
import com.comercialvalerio.application.dto.TipoPedido;
import com.comercialvalerio.application.mapper.PresentacionDtoMapper;
import com.comercialvalerio.application.mapper.ProductoDtoMapper;
import com.comercialvalerio.application.mapper.ProductoMasVendidoDtoMapper;
import com.comercialvalerio.application.mapper.TallaStockDtoMapper;
import com.comercialvalerio.application.service.MovimientoInventarioService;
import com.comercialvalerio.application.service.ProductoService;
import com.comercialvalerio.application.service.util.SecurityChecks;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.model.Estado;
import com.comercialvalerio.domain.model.EstadoNombre;
import com.comercialvalerio.domain.model.Presentacion;
import com.comercialvalerio.domain.model.Producto;
import com.comercialvalerio.domain.model.TallaStock;
import com.comercialvalerio.domain.model.TipoProducto;
import com.comercialvalerio.domain.model.TipoProductoNombre;
import com.comercialvalerio.domain.repository.CategoriaRepository;
import com.comercialvalerio.domain.repository.MovimientoInventarioRepository;
import com.comercialvalerio.domain.repository.PresentacionRepository;
import com.comercialvalerio.domain.repository.ProductoMasVendidoRepository;
import com.comercialvalerio.domain.repository.ProductoRepository;
import com.comercialvalerio.domain.repository.TallaStockRepository;
import com.comercialvalerio.domain.repository.TipoMovimientoRepository;
import com.comercialvalerio.domain.repository.TipoProductoRepository;
import com.comercialvalerio.domain.service.GestionProductoService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Implementación de {@link ProductoService}.
 */
@ApplicationScoped
@Transactional
public class ProductoServiceImpl implements ProductoService {

    /* ===== INYECCIONES ===== */
    private final ProductoRepository     repoProd;
    private final CategoriaRepository    repoCat;
    private final TipoProductoRepository repoTipo;
    private final EstadoCache            estadoCache;
    private final TallaStockRepository   repoTalla;
    private final PresentacionRepository repoPres;
    private final GestionProductoService  gestionSvc;
    private final ProductoMasVendidoRepository repoMasVend;
    private final MovimientoInventarioRepository repoMov;
    private final MovimientoInventarioService movSvc;
    private final TipoMovimientoRepository repoTipoMov;
@Inject
    ProductoDtoMapper mapper;

    @Inject
    ProductoMasVendidoDtoMapper masVendMapper;
    @Inject
    TallaStockDtoMapper tallaMapper;
    @Inject
    PresentacionDtoMapper presMapper;

    @Inject
    public ProductoServiceImpl(ProductoRepository repoProd,
                               CategoriaRepository repoCat,
                               TipoProductoRepository repoTipo,
                               EstadoCache estadoCache,
                               TallaStockRepository repoTalla,
                               PresentacionRepository repoPres,
                               GestionProductoService gestionSvc,
                               ProductoMasVendidoRepository repoMasVend,
                               MovimientoInventarioRepository repoMov,
                               MovimientoInventarioService movSvc,
                               TipoMovimientoRepository repoTipoMov) {
        this.repoProd  = repoProd;
        this.repoCat   = repoCat;
        this.repoTipo  = repoTipo;
        this.estadoCache = estadoCache;
        this.repoTalla = repoTalla;
        this.repoPres  = repoPres;
        this.gestionSvc = gestionSvc;
        this.repoMasVend = repoMasVend;
        this.repoMov = repoMov;
        this.movSvc = movSvc;
        this.repoTipoMov = repoTipoMov;
    }

    /* ====== LECTURA ====== */
    @Override
    public List<ProductoDto> listar(String nombre, Integer idCategoria,
                                    Integer idTipoProducto,
                                    String talla, String unidad) {
        if (nombre == null && idCategoria == null && idTipoProducto == null &&
            talla == null && unidad == null) {
            return ServiceUtils.mapList(repoProd.findAll(), mapper::toDto);
        }
        return ServiceUtils.mapList(
                repoProd.findByFiltros(nombre, idCategoria, idTipoProducto, talla, unidad),
                mapper::toDto);
    }
    @Override public List<ProductoDto> listarBajoStock() {
        return ServiceUtils.mapList(repoProd.findBajoStock(), mapper::toDto);
    }

    @Override
    public List<ProductoDto> listarParaPedido(String nombre, TipoPedido tipoPedidoDefault) {
        com.comercialvalerio.domain.model.TipoPedido dom = tipoPedidoDefault == null
                ? null
                : com.comercialvalerio.domain.model.TipoPedido.valueOf(tipoPedidoDefault.name());
        return ServiceUtils.mapList(
                repoProd.findParaPedido(nombre, dom),
                mapper::toDto);
    }

    @Override
    public List<ProductoVentaDto> listarParaVenta() {
        List<Producto> prods = repoProd.findWithTallasAndPresentaciones();
        return ServiceUtils.mapList(prods, p -> new ProductoVentaDto(
                p.getIdProducto(),
                p.getNombre(),
                p.getUnidadMedida(),
                p.getPrecioUnitario(),
                p.getStockActual(),
                ServiceUtils.mapList(p.getTallas(), tallaMapper::toDto),
                ServiceUtils.mapList(p.getPresentaciones(), presMapper::toDto)
        ));
    }

    @Override
    public List<ProductoMasVendidoDto> listarMasVendidos(int limite) {
        return ServiceUtils.mapList(repoMasVend.top(limite), masVendMapper::toDto);
    }
    @Override public ProductoDto obtener(Integer id) {
        Producto p = ServiceChecks.requireFound(
                repoProd.findById(id), "Producto no encontrado");
        return mapper.toDto(p);
    }

    /* ====== ALTA ====== */
    @Override public ProductoDto crear(ProductoCUDto in) {
        return saveOrUpdate(null, in);
    }

    /* ====== MODIFICACIÓN ====== */
    @Override public ProductoDto actualizar(Integer id, ProductoCUDto in) {
        return saveOrUpdate(id, in);
    }

    /* ====== BAJA ====== */
    @Override
    public void eliminar(Integer id) {
        SecurityChecks.requireAdminRole();
        gestionSvc.eliminarProducto(id);
    }

    /* ====== STOCK DIRECTO ====== */
    @Override public void ajustarStock(Integer idProducto, BigDecimal s) {
        SecurityChecks.requireAdminRole();
        repoProd.actualizarStock(idProducto, s);
    }

    @Override
    public void cambiarEstado(Integer idProducto, CambiarEstadoDto dto) {
        Producto prod = ServiceChecks.requireFound(
                repoProd.findById(idProducto), "Producto no encontrado");
        Estado est = estadoCache.get("Producto", dto.nuevoEstado());
        // Al desactivar el producto también se desactivan sus tallas y presentaciones
        if (EstadoNombre.INACTIVO.getNombre().equalsIgnoreCase(dto.nuevoEstado()) ||
            EstadoNombre.DESACTIVADO.getNombre().equalsIgnoreCase(dto.nuevoEstado())) {
            for (TallaStock ts : repoTalla.findByProducto(idProducto)) {
                repoTalla.updateEstado(ts.getIdTallaStock(),
                        EstadoNombre.INACTIVO.getNombre());
            }
            for (Presentacion pr : repoPres.findByProducto(idProducto)) {
                repoPres.updateEstado(pr.getIdPresentacion(),
                        EstadoNombre.INACTIVO.getNombre());
            }
        }
        prod.setEstado(est);
        repoProd.updateEstado(idProducto, est.getNombre());

        if (EstadoNombre.ACTIVO.getNombre().equalsIgnoreCase(dto.nuevoEstado())) {
            var categoria = prod.getCategoria();
            if (categoria != null && categoria.getEstado() != null &&
                    EstadoNombre.INACTIVO.getNombre()
                            .equalsIgnoreCase(categoria.getEstado().getNombre())) {
                Estado act = estadoCache.get("Categoria", EstadoNombre.ACTIVO);
                categoria.setEstado(act);
                repoCat.cambiarEstado(categoria.getIdCategoria(), act.getNombre(), false);
            }
        }
    }

    @Override
    public void recalcularStockGlobal() {
        gestionSvc.recalcularStockGlobal();
    }

    @Override
    public List<String> obtenerDependencias(Integer idProducto) {
        return gestionSvc.findDependencias(idProducto);
    }

    /* ================================================================
       M E T O D O   C O M Ú N  (Create | Update)
       ================================================================= */
    private ProductoDto saveOrUpdate(Integer id, ProductoCUDto in) {
        boolean updating = id != null;

        ProductoValidator.FkRefs fks = validateInput(in, updating);
        BigDecimal stock = deriveStock(in, fks.tipo());

        Producto prod = createOrLoadProducto(id);

        applyUpdates(prod, in, fks, stock, updating);

        gestionSvc.guardarProducto(prod);

        syncSubcollections(prod, in, fks.tipo(), !updating);

        if (!updating) {
            registerInitialMovements(prod, fks.tipo(), stock, in);
        } else {
            var nuevas = filterNewTallas(in.tallas());
            if (!nuevas.isEmpty()) {
                ProductoCrudHelper.registrarMovimientosIniciales(
                        prod, fks.tipo(), null, nuevas,
                        movSvc, repoTalla, repoTipoMov);
            }
        }

        return mapper.toDto(prod);
    }

    private ProductoValidator.FkRefs validateInput(ProductoCUDto in, boolean updating) {
        ProductoValidator.FkRefs fks = ProductoValidator.validateForeignKeys(in, repoCat, repoTipo, estadoCache);
        ProductoValidator.validarPrecioStock(in, fks.tipo(), updating);
        ProductoValidator.validarTipoPedido(in);
        return fks;
    }

    private BigDecimal deriveStock(ProductoCUDto in, TipoProducto tipo) {
        return ProductoCrudHelper.deriveStock(in, tipo);
    }

    private Producto createOrLoadProducto(Integer id) {
        Producto prod = id == null ? new Producto() :
                ServiceChecks.requireFound(repoProd.findById(id),
                        "Producto no encontrado");
        return prod;
    }

    private void syncSubcollections(Producto prod, ProductoCUDto in, TipoProducto tipo, boolean nuevoProducto) {
        ProductoCrudHelper.syncSubcollections(prod, in, tipo, nuevoProducto,
                repoTalla, repoPres, repoMov, estadoCache);
    }

    private void registerInitialMovements(Producto prod, TipoProducto tipo, BigDecimal stock, ProductoCUDto in) {
        ProductoCrudHelper.registrarMovimientosIniciales(prod, tipo, stock, in.tallas(),
                movSvc, repoTalla, repoTipoMov);
    }

    private java.util.List<TallaStockCUDto> filterNewTallas(java.util.List<TallaStockCUDto> tallas) {
        if (tallas == null) return java.util.List.of();
        return tallas.stream()
                .filter(t -> t.idTallaStock() == null)
                .toList();
    }

    private void applyUpdates(Producto prod, ProductoCUDto in,
                              ProductoValidator.FkRefs fks, BigDecimal stock, boolean updating) {
        prod.setNombre(in.nombre());
        prod.setDescripcion(in.descripcion());
        prod.setCategoria(fks.cat());
        if (!updating)
            prod.setTipoProducto(fks.tipo());
        prod.setUnidadMedida(in.unidadMedida());
        TipoProductoNombre tipoNombre = TipoProductoNombre.fromNombre(
                fks.tipo().getNombre());
        if (tipoNombre == TipoProductoNombre.FRACCIONABLE) {
            prod.setPrecioUnitario(null);
        } else {
            prod.setPrecioUnitario(in.precioUnitario());
        }
        if (in.mayorista()) {
            prod.setMayorista(true, in.minMayorista(), in.precioMayorista());
        } else {
            prod.setMayorista(false);
        }
        prod.setParaPedido(in.paraPedido());
        if (in.tipoPedidoDefault() != null) {
            prod.setTipoPedidoDefault(
                com.comercialvalerio.domain.model.TipoPedido.valueOf(
                    in.tipoPedidoDefault().name()));
        } else {
            prod.setTipoPedidoDefault(null);
        }
        if (!updating) {
            if (!TipoProductoNombre.VESTIMENTA.equalsNombre(fks.tipo().getNombre())) {
                prod.setStockActual(stock);
            } else {
                prod.setStockActual(null);
            }
        }
        prod.setUmbral(in.umbral());
        if (!updating)
            prod.setEstado(fks.estActivo());
    }

    /* ===================================================== */
}

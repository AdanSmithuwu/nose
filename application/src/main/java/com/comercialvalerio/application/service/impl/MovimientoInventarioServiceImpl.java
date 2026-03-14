package com.comercialvalerio.application.service.impl;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

import com.comercialvalerio.application.dto.MovimientoInventarioCreateDto;
import com.comercialvalerio.application.dto.MovimientoInventarioDto;
import com.comercialvalerio.application.mapper.MovimientoInventarioDtoMapper;
import com.comercialvalerio.application.service.MovimientoInventarioService;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.application.service.util.SecurityChecks;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.model.Empleado;
import com.comercialvalerio.domain.model.MovimientoInventario;
import com.comercialvalerio.domain.model.Producto;
import com.comercialvalerio.domain.model.Presentacion;
import com.comercialvalerio.domain.model.TallaStock;
import com.comercialvalerio.domain.model.TipoMovimiento;
import com.comercialvalerio.domain.model.TipoMovimientoNombre;
import com.comercialvalerio.domain.model.TipoProductoNombre;
import com.comercialvalerio.domain.model.RolNombre;
import com.comercialvalerio.domain.repository.EmpleadoRepository;
import com.comercialvalerio.domain.repository.MovimientoInventarioRepository;
import com.comercialvalerio.domain.repository.ProductoRepository;
import com.comercialvalerio.domain.repository.PresentacionRepository;
import com.comercialvalerio.domain.repository.TallaStockRepository;
import com.comercialvalerio.domain.repository.TipoMovimientoRepository;
import com.comercialvalerio.domain.service.InventarioService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class MovimientoInventarioServiceImpl
        implements MovimientoInventarioService {

    private final MovimientoInventarioRepository repoMov;
    private final ProductoRepository             repoProd;
    private final PresentacionRepository         repoPres;
    private final TallaStockRepository           repoTalla;
    private final TipoMovimientoRepository       repoTipo;
    private final EmpleadoRepository             repoEmp;
    private final InventarioService inventarioSvc;
@Inject
    MovimientoInventarioDtoMapper mapper;

    @Inject
    public MovimientoInventarioServiceImpl(
            MovimientoInventarioRepository repoMov,
            ProductoRepository             repoProd,
            PresentacionRepository         repoPres,
            TallaStockRepository           repoTalla,
            TipoMovimientoRepository       repoTipo,
            EmpleadoRepository             repoEmp,
            InventarioService inventarioSvc) {
        this.repoMov  = repoMov;
        this.repoProd = repoProd;
        this.repoPres = repoPres;
        this.repoTalla= repoTalla;
        this.repoTipo = repoTipo;
        this.repoEmp  = repoEmp;
        this.inventarioSvc = inventarioSvc;
    }

    /* ---------- Lectura ---------- */
    @Override
    public List<MovimientoInventarioDto> listarPorProducto(Integer id) {
        SecurityChecks.requireAdminRole();
        return ServiceUtils.mapList(repoMov.findByProducto(id), mapper::toDto);
    }

    @Override
    public List<MovimientoInventarioDto> listarPorRango(LocalDateTime d,
                                                        LocalDateTime h) {
        SecurityChecks.requireAdminRole();
        return ServiceUtils.mapList(repoMov.findByRangoFecha(d, h), mapper::toDto);
    }

    /* ---------- Escritura ---------- */
    @Override
    public MovimientoInventarioDto registrar(MovimientoInventarioCreateDto dto) {

        Producto prod = ServiceChecks.requireFound(
                repoProd.findById(dto.idProducto()), "Producto inexistente");

        TallaStock ts = null;
        if (dto.idTallaStock() != null) {
            ts = ServiceChecks.requireFound(
                    repoTalla.findById(dto.idTallaStock()),
                    "TallaStock inexistente");
        }

        if (TipoProductoNombre.FRACCIONABLE.equalsNombre(prod.getTipoProducto().getNombre())) {
            List<Presentacion> pres = repoPres.findByProducto(prod.getIdProducto());
            boolean match = pres.stream()
                    .anyMatch(p -> {
                        BigDecimal c = p.getCantidad();
                        BigDecimal cant = dto.cantidad();
                        return c != null && cant != null &&
                               cant.remainder(c).compareTo(BigDecimal.ZERO) == 0;
                    });
            if (!match) {
                throw new BusinessRuleViolationException(
                        "La cantidad no coincide con ninguna presentación registrada");
            }
        }

        TipoMovimiento tm = ServiceChecks.requireFound(
                repoTipo.findById(dto.idTipoMov()),
                "TipoMovimiento inexistente");

        Empleado emp = ServiceChecks.requireFound(
                repoEmp.findById(dto.idEmpleado()), "Empleado inexistente");

        boolean esAjuste = TipoMovimientoNombre.fromNombre(
                tm.getNombre()) == TipoMovimientoNombre.AJUSTE;
        if (esAjuste) {
            String rol = emp.getRol() != null ? emp.getRol().getNombre() : null;
            RolNombre rolNombre;
            try {
                rolNombre = RolNombre.fromNombre(rol);
            } catch (IllegalArgumentException | NullPointerException ex) {
                rolNombre = null;
            }
            if (rolNombre != RolNombre.ADMINISTRADOR) {
                throw new BusinessRuleViolationException(
                        "Solo un administrador puede ajustar el stock");
            }
            if (dto.motivo() == null || dto.motivo().isBlank()) {
                throw new BusinessRuleViolationException(
                        "El motivo es obligatorio para movimientos de ajuste");
            }
        }

        MovimientoInventario mov = MovimientoInventario.crear(
                prod,
                ts,
                tm,
                dto.cantidad(),
                dto.motivo(),
                emp);

        inventarioSvc.registrarMovimiento(mov);
        return mapper.toDto(mov);
    }
}

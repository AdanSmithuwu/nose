package com.comercialvalerio.application.service.impl;
import java.util.List;

import com.comercialvalerio.application.dto.PresentacionCreateDto;
import com.comercialvalerio.application.dto.PresentacionDto;
import com.comercialvalerio.application.mapper.PresentacionDtoMapper;
import com.comercialvalerio.application.mapper.SubcollectionMapper;
import com.comercialvalerio.application.service.PresentacionService;
import com.comercialvalerio.application.cache.EstadoCache;
import com.comercialvalerio.application.service.util.SecurityChecks;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.model.EstadoNombre;
import com.comercialvalerio.domain.model.Estado;
import com.comercialvalerio.domain.model.Presentacion;
import com.comercialvalerio.domain.model.Producto;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.repository.PresentacionRepository;
import com.comercialvalerio.domain.repository.ProductoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class PresentacionServiceImpl implements PresentacionService {

    private final PresentacionRepository repoPre;
    private final ProductoRepository     repoProd;
    private final EstadoCache           estadoCache;
@Inject
    PresentacionDtoMapper mapper;

    @Inject
    public PresentacionServiceImpl(PresentacionRepository repoPre,
                                   ProductoRepository     repoProd,
                                   EstadoCache           estadoCache) {
        this.repoPre    = repoPre;
        this.repoProd   = repoProd;
        this.estadoCache = estadoCache;
    }

    private void checkDuplicado(Integer idProd, java.math.BigDecimal cant, Integer idActual) {
        boolean dup = repoPre.findByProducto(idProd).stream()
                .anyMatch(p -> p.getCantidad().compareTo(cant) == 0 &&
                             !p.getIdPresentacion().equals(idActual));
        if (dup) {
            throw new DuplicateEntityException(
                    "Ya existe una presentación con esa cantidad para el producto");
        }
    }

    @Override public List<PresentacionDto> listarPorProducto(Integer idProd) {
        return ServiceUtils.mapList(
                repoPre.findByProducto(idProd).stream()
                      .filter(p -> EstadoNombre.ACTIVO.equalsNombre(
                              p.getEstado().getNombre()))
                      .toList(),
                mapper::toDto);
    }

    @Override public PresentacionDto obtener(Integer id) {
        Presentacion p = ServiceChecks.requireFound(
                repoPre.findById(id), "Presentación no encontrada");
        return mapper.toDto(p);
    }

    @Override public PresentacionDto crear(PresentacionCreateDto in) {
        Producto prod = ServiceChecks.requireFound(
                repoProd.findById(in.idProducto()), "Producto inexistente");

        checkDuplicado(in.idProducto(), in.cantidad(), null);
        Presentacion p = SubcollectionMapper.fromDto(in, prod);
        if (p.getEstado() == null) {
            Estado activo = estadoCache.get("Producto", EstadoNombre.ACTIVO);
            p.setEstado(activo);
        }
        repoPre.save(p);
        return mapper.toDto(p);
    }

    @Override public PresentacionDto actualizar(Integer id, PresentacionCreateDto in) {
        Presentacion p = ServiceChecks.requireFound(
                repoPre.findById(id), "Presentación no encontrada");

        Producto prod = ServiceChecks.requireFound(
                repoProd.findById(in.idProducto()), "Producto inexistente");

        checkDuplicado(in.idProducto(), in.cantidad(), id);
        SubcollectionMapper.updateFromDto(in, prod, p);
        repoPre.save(p);
        return mapper.toDto(p);
    }

    @Override
    public void eliminar(Integer id) {
        SecurityChecks.requireAdminRole();
        repoPre.delete(id);
    }

    @Override public void activar(Integer id) {
        repoPre.updateEstado(id, EstadoNombre.ACTIVO.getNombre());
    }

    @Override public void desactivar(Integer id) {
        repoPre.updateEstado(id, EstadoNombre.INACTIVO.getNombre());
    }

    @Override
    public List<PresentacionDto> listarTodosPorProducto(Integer idProd) {
        return ServiceUtils.mapList(repoPre.findByProducto(idProd), mapper::toDto);
    }
}

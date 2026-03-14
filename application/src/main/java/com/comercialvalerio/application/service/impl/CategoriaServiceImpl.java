package com.comercialvalerio.application.service.impl;
import java.util.List;
import java.util.ArrayList;
import com.comercialvalerio.common.DependencyUtils;

import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.dto.CategoriaCreateDto;
import com.comercialvalerio.application.dto.CategoriaDto;
import com.comercialvalerio.application.mapper.CategoriaDtoMapper;
import com.comercialvalerio.application.service.CategoriaService;
import com.comercialvalerio.application.service.util.SecurityChecks;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.model.Categoria;
import com.comercialvalerio.domain.model.Estado;
import com.comercialvalerio.domain.model.EstadoNombre;
import com.comercialvalerio.domain.repository.CategoriaRepository;
import com.comercialvalerio.application.cache.EstadoCache;
import com.comercialvalerio.domain.repository.ProductoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;

@ApplicationScoped
@Transactional
public class CategoriaServiceImpl implements CategoriaService {
    private final CategoriaRepository repo;
    private final EstadoCache       estadoCache;
    private final ProductoRepository repoProd;
@Inject
    CategoriaDtoMapper mapper;

    @Inject
    public CategoriaServiceImpl(CategoriaRepository repo, EstadoCache estadoCache,
                                ProductoRepository repoProd) {
        this.repo = repo;
        this.estadoCache = estadoCache;
        this.repoProd = repoProd;
    }

    @Override public List<CategoriaDto> listar() {
        return ServiceUtils.mapList(repo.findAll(), mapper::toDto);
    }

    @Override public CategoriaDto obtener(Integer id) {
        Categoria c = ServiceChecks.requireFound(
                repo.findById(id), "Categoría no encontrada");
        return mapper.toDto(c);
    }

    @Override public CategoriaDto crear(CategoriaCreateDto dto) {
        SecurityChecks.requireAdminRole();
        if (dto.nombre() == null || dto.nombre().isBlank()) {
            throw new BusinessRuleViolationException("El nombre de la categoría es obligatorio");
        }
        if (repo.existsByNombre(dto.nombre(), null)) {
            throw new DuplicateEntityException(
                    "Ya existe la categoría «" + dto.nombre() + "»");
        }
        Estado activo = estadoCache.get("Categoria", EstadoNombre.ACTIVO);
        Categoria c = mapper.toModel(dto);
        c.setEstado(activo);
        repo.save(c);
        return mapper.toDto(c);
    }

    @Override public CategoriaDto actualizar(Integer id, CategoriaCreateDto dto) {
        SecurityChecks.requireAdminRole();
        if (dto.nombre() == null || dto.nombre().isBlank()) {
            throw new BusinessRuleViolationException("El nombre de la categoría es obligatorio");
        }
        if (repo.existsByNombre(dto.nombre(), id)) {
            throw new DuplicateEntityException(
                    "Ya existe la categoría «" + dto.nombre() + "»");
        }
        Categoria c = ServiceChecks.requireFound(
                repo.findById(id), "Categoría no encontrada");
        c.setNombre(dto.nombre());
        c.setDescripcion(dto.descripcion());
        repo.save(c);
        return mapper.toDto(c);
    }

    @Override
    public void eliminar(Integer id) {
        SecurityChecks.requireAdminRole();
        repo.delete(id);
    }

    @Override
    public int cambiarEstado(Integer id, CambiarEstadoDto dto, boolean actualizarProductos) {
        SecurityChecks.requireAdminRole();

        Categoria cat = ServiceChecks.requireFound(
                repo.findById(id), "Categoría no encontrada");

        Estado est = estadoCache.get("Categoria", dto.nuevoEstado());

        cat.setEstado(est);
        return repo.cambiarEstado(id, est.getNombre(), actualizarProductos);
    }

    @Override
    public List<String> obtenerDependencias(Integer idCategoria) {
        List<String> deps = new ArrayList<>();
        DependencyUtils.addIf(repoProd.existsByCategoria(idCategoria), "productos", deps);
        return deps;
    }
}

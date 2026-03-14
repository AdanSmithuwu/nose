package com.comercialvalerio.application.service.impl;
import java.util.List;

import com.comercialvalerio.application.dto.TipoMovimientoCreateDto;
import com.comercialvalerio.application.dto.TipoMovimientoDto;
import com.comercialvalerio.application.mapper.TipoMovimientoDtoMapper;
import com.comercialvalerio.application.service.TipoMovimientoService;
import com.comercialvalerio.application.service.util.SecurityChecks;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.model.TipoMovimiento;
import com.comercialvalerio.domain.repository.TipoMovimientoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class TipoMovimientoServiceImpl implements TipoMovimientoService {

    private final TipoMovimientoRepository repo;
@Inject
    TipoMovimientoDtoMapper mapper;

    @Inject
    public TipoMovimientoServiceImpl(TipoMovimientoRepository repo){ this.repo = repo; }

    @Override public List<TipoMovimientoDto> listar() {
        return ServiceUtils.mapList(repo.findAll(), mapper::toDto);
    }

    @Override public TipoMovimientoDto obtener(Integer id) {
        TipoMovimiento t = ServiceChecks.requireFound(
                repo.findById(id), "TipoMovimiento no encontrado");
        return mapper.toDto(t);
    }

    @Override
    public TipoMovimientoDto buscarPorNombre(String nombre) {
        TipoMovimiento t = ServiceChecks.requireFound(
                repo.findByNombre(nombre),
                "Tipo de movimiento con nombre '" + nombre + "' no existe");
        return mapper.toDto(t);
    }

    @Override public TipoMovimientoDto crear(TipoMovimientoCreateDto dto) {
        if (repo.findByNombre(dto.nombre()).isPresent())
            throw new DuplicateEntityException("Nombre duplicado");
        TipoMovimiento t = new TipoMovimiento();
        t.setNombre(dto.nombre());
        repo.save(t);
        return mapper.toDto(t);
    }

    @Override public TipoMovimientoDto actualizar(Integer id, TipoMovimientoCreateDto dto) {
        TipoMovimiento t = ServiceChecks.requireFound(
                repo.findById(id), "TipoMovimiento no encontrado");
        java.util.Optional<TipoMovimiento> dup = repo.findByNombre(dto.nombre());
        if (dup.isPresent() && !dup.get().getIdTipoMovimiento().equals(id))
            throw new DuplicateEntityException("Nombre duplicado");
        t.setNombre(dto.nombre());
        repo.save(t);
        return mapper.toDto(t);
    }

    @Override
    public void eliminar(Integer id) {
        SecurityChecks.requireAdminRole();
        repo.delete(id);
    }
}

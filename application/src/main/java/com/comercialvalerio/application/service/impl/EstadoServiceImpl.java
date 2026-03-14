package com.comercialvalerio.application.service.impl;
import java.util.List;

import com.comercialvalerio.application.dto.EstadoCreateDto;
import com.comercialvalerio.application.dto.EstadoDto;
import com.comercialvalerio.application.mapper.EstadoDtoMapper;
import com.comercialvalerio.application.service.EstadoService;
import com.comercialvalerio.application.service.util.SecurityChecks;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.model.Estado;
import com.comercialvalerio.domain.repository.EstadoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class EstadoServiceImpl implements EstadoService {

    private final EstadoRepository repo;
@Inject
    EstadoDtoMapper mapper;

    @Inject
    public EstadoServiceImpl(EstadoRepository repo) { this.repo = repo; }

    @Override
    public List<EstadoDto> listar() {
        return ServiceUtils.mapList(repo.findAll(), mapper::toDto);
    }

    @Override
    public EstadoDto obtener(Integer id) {
        Estado e = ServiceChecks.requireFound(
                repo.findById(id), "Estado no encontrado");
        return mapper.toDto(e);
    }

    @Override
    public EstadoDto crear(EstadoCreateDto dto) {
        Estado e = mapper.toModel(dto);
        repo.save(e);
        return mapper.toDto(e);
    }

    @Override
    public EstadoDto actualizar(Integer id, EstadoCreateDto dto) {
        Estado e = ServiceChecks.requireFound(
                repo.findById(id), "Estado no encontrado");
        e.setNombre(dto.nombre());
        e.setModulo(dto.modulo());
        repo.save(e);
        return mapper.toDto(e);
    }

    @Override
    public void eliminar(Integer id) {
        SecurityChecks.requireAdminRole();
        repo.delete(id);
    }

    @Override
    public EstadoDto buscarPorModuloYNombre(String modulo, String nombre) {
        Estado e = ServiceChecks.requireFound(
                repo.findByModuloAndNombre(modulo, nombre),
                String.format("No existe estado «%s» en módulo «%s»", nombre, modulo));
        return mapper.toDto(e);
    }
}

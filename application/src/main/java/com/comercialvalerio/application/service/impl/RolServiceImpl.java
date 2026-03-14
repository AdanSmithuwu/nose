package com.comercialvalerio.application.service.impl;
import java.util.List;

import com.comercialvalerio.application.dto.RolDto;
import com.comercialvalerio.application.mapper.RolDtoMapper;
import com.comercialvalerio.application.service.RolService;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.model.Rol;
import com.comercialvalerio.domain.repository.RolRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class RolServiceImpl implements RolService {

    private final RolRepository repo;
@Inject
    RolDtoMapper mapper;

    @Inject
    public RolServiceImpl(RolRepository repo) { this.repo = repo; }

    @Override
    public List<RolDto> listar() {
        return ServiceUtils.mapList(repo.findAll(), mapper::toDto);
    }

    @Override
    public RolDto obtener(Integer id) {
        Rol r = ServiceChecks.requireFound(
                repo.findById(id), "Rol no encontrado");
        return mapper.toDto(r);
    }

    @Override
    public RolDto buscarPorNombre(String nombre) {
        Rol r = ServiceChecks.requireFound(
                repo.findByNombre(nombre),
                "Rol no encontrado con nombre «" + nombre + "»");
        return mapper.toDto(r);
    }
}

package com.comercialvalerio.application.service.impl;
import java.util.List;

import com.comercialvalerio.application.dto.TipoProductoCreateDto;
import com.comercialvalerio.application.dto.TipoProductoDto;
import com.comercialvalerio.application.mapper.TipoProductoDtoMapper;
import com.comercialvalerio.application.service.TipoProductoService;
import com.comercialvalerio.application.service.util.SecurityChecks;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.model.TipoProducto;
import com.comercialvalerio.domain.repository.TipoProductoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class TipoProductoServiceImpl implements TipoProductoService {

    private final TipoProductoRepository repo;
@Inject
    TipoProductoDtoMapper mapper;

    @Inject
    public TipoProductoServiceImpl(TipoProductoRepository repo) { this.repo = repo; }

    @Override public List<TipoProductoDto> listar() {
        return ServiceUtils.mapList(repo.findAll(), mapper::toDto);
    }

    @Override public TipoProductoDto obtener(Integer id) {
        TipoProducto tp = ServiceChecks.requireFound(
                repo.findById(id), "Tipo de producto no encontrado");
        return mapper.toDto(tp);
    }

    @Override
    public TipoProductoDto buscarPorNombre(String nombre) {
        TipoProducto tp = ServiceChecks.requireFound(
                repo.findByNombre(nombre),
                "Tipo de producto con nombre '" + nombre + "' no existe");
        return mapper.toDto(tp);
    }

    @Override public TipoProductoDto crear(TipoProductoCreateDto dto) {
        TipoProducto tp = mapper.toModel(dto);
        repo.save(tp);
        return mapper.toDto(tp);
    }

    @Override public TipoProductoDto actualizar(Integer id, TipoProductoCreateDto dto) {
        TipoProducto tp = ServiceChecks.requireFound(
                repo.findById(id), "Tipo de producto no encontrado");
        tp.setNombre(dto.nombre());
        repo.save(tp);
        return mapper.toDto(tp);
    }

    @Override
    public void eliminar(Integer id) {
        SecurityChecks.requireAdminRole();
        repo.delete(id);
    }
}

package com.comercialvalerio.application.service.impl;
import java.util.List;

import com.comercialvalerio.application.dto.MetodoPagoCreateDto;
import com.comercialvalerio.application.dto.MetodoPagoDto;
import com.comercialvalerio.application.mapper.MetodoPagoDtoMapper;
import com.comercialvalerio.application.service.MetodoPagoService;
import com.comercialvalerio.application.service.util.SecurityChecks;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.model.MetodoPago;
import com.comercialvalerio.domain.repository.MetodoPagoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class MetodoPagoServiceImpl implements MetodoPagoService {

    private final MetodoPagoRepository repo;
@Inject
    MetodoPagoDtoMapper mapper;

    @Inject
    public MetodoPagoServiceImpl(MetodoPagoRepository repo) { this.repo = repo; }

    @Override
    public List<MetodoPagoDto> listar() {
        return ServiceUtils.mapList(repo.findAll(), mapper::toDto);
    }

    @Override
    public MetodoPagoDto obtener(Integer id) {
        MetodoPago m = ServiceChecks.requireFound(
                repo.findById(id), "Método de pago no encontrado");
        return mapper.toDto(m);
    }

    @Override
    public MetodoPagoDto buscarPorNombre(String nombre) {
        MetodoPago m = ServiceChecks.requireFound(
                repo.findByNombre(nombre),
                "Método de pago con nombre '" + nombre + "' no existe");
        return mapper.toDto(m);
    }

    @Override
    public MetodoPagoDto crear(MetodoPagoCreateDto dto) {
        MetodoPago m = mapper.toModel(dto);
        repo.save(m);
        return mapper.toDto(m);
    }

    @Override
    public MetodoPagoDto actualizar(Integer id, MetodoPagoCreateDto dto) {
        MetodoPago m = ServiceChecks.requireFound(
                repo.findById(id), "Método de pago no encontrado");
        m.setNombre(dto.nombre());
        repo.save(m);
        return mapper.toDto(m);
    }

    @Override
    public void eliminar(Integer id) {
        SecurityChecks.requireAdminRole();
        repo.delete(id);
    }
}

package com.comercialvalerio.application.service.impl;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import com.comercialvalerio.common.DependencyUtils;

import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.dto.ClienteCreateDto;
import com.comercialvalerio.application.dto.ClienteDto;
import com.comercialvalerio.application.mapper.ClienteDtoMapper;
import com.comercialvalerio.application.service.ClienteService;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.model.Cliente;
import com.comercialvalerio.domain.model.Estado;
import com.comercialvalerio.domain.model.EstadoNombre;
import com.comercialvalerio.domain.repository.ClienteRepository;
import com.comercialvalerio.application.cache.EstadoCache;
import com.comercialvalerio.domain.repository.PagoTransaccionRepository;
import com.comercialvalerio.domain.repository.PedidoRepository;
import com.comercialvalerio.domain.repository.VentaRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repo;
    private final EstadoCache       estadoCache;
    private final VentaRepository   repoVenta;
    private final PedidoRepository  repoPedido;
    private final PagoTransaccionRepository repoPago;
    @Inject
    ClienteDtoMapper mapper;

    @Inject
    public ClienteServiceImpl(ClienteRepository repo, EstadoCache estadoCache,
                              VentaRepository repoVenta,
                              PedidoRepository repoPedido,
                              PagoTransaccionRepository repoPago) {
        this.repo       = repo;
        this.estadoCache = estadoCache;
        this.repoVenta  = repoVenta;
        this.repoPedido = repoPedido;
        this.repoPago   = repoPago;
    }

    @Override public List<ClienteDto> listar() {
        return ServiceUtils.mapList(repo.findAll(), mapper::toDto);
    }

    @Override public List<ClienteDto> listarActivos() {
        return ServiceUtils.mapList(repo.findActivos(), mapper::toDto);
    }

    @Override public List<ClienteDto> findByEstado(String nombre) {
        return ServiceUtils.mapList(repo.findByEstado(nombre), mapper::toDto);
    }

    @Override public ClienteDto obtener(Integer id) {
        Cliente c = ServiceChecks.requireFound(
                repo.findById(id), "Cliente no encontrado");
        return mapper.toDto(c);
    }

    @Override
    public ClienteDto obtenerPorDni(String dni) {
        Cliente c = ServiceChecks.requireFound(
                repo.findByDni(dni), "Cliente con DNI " + dni + " no encontrado");
        return mapper.toDto(c);
    }

    @Override
    public List<ClienteDto> buscarPorNombre(String patron) {
        return ServiceUtils.mapList(repo.findByNombreLike(patron), mapper::toDto);
    }

    @Override
    public List<ClienteDto> buscarPorTelefono(String numero) {
        return ServiceUtils.mapList(repo.findByTelefono(numero), mapper::toDto);
    }

    @Override
    public List<ClienteDto> listarPorRangoRegistro(LocalDate desde, LocalDate hasta) {
        return ServiceUtils.mapList(repo.findByRangoRegistro(desde, hasta), mapper::toDto);
    }

    @Override public ClienteDto registrar(ClienteCreateDto in) {
        Cliente c = mapper.toModel(in);
        c.setFechaRegistro(LocalDate.now());
        if (c.getEstado() == null) {
            Estado activo = estadoCache.get("Persona", EstadoNombre.ACTIVO);
            c.setEstado(activo);
        }
        repo.save(c);
        return mapper.toDto(c);
    }

    @Override public ClienteDto actualizar(Integer id, ClienteCreateDto chg) {
        Cliente c = ServiceChecks.requireFound(
                repo.findById(id), "Cliente no encontrado");

        c.setDni(chg.dni());
        c.setNombres(chg.nombres());
        c.setApellidos(chg.apellidos());
        c.setTelefono(chg.telefono());
        c.setDireccion(chg.direccion());

        try {
            repo.save(c);
        } catch (DuplicateEntityException ex) {
            // Propagar error de DNI duplicado hacia la capa REST
            throw ex;
        }

        return mapper.toDto(c);
    }

    @Override
    public void eliminar(Integer id) {
        repo.delete(id);
    }

    @Override
    public void cambiarEstado(Integer id, CambiarEstadoDto dto) {
        Cliente cli = ServiceChecks.requireFound(
                repo.findById(id), "Cliente no encontrado");
        Estado est = estadoCache.get("Persona", dto.nuevoEstado());
        cli.setEstado(est);
        repo.updateEstado(id, est.getNombre());
    }

    @Override
    public List<String> obtenerDependencias(Integer idCliente) {
        List<String> deps = new ArrayList<>();
        DependencyUtils.addIf(!repoVenta.findByCliente(idCliente).isEmpty(), "ventas", deps);
        DependencyUtils.addIf(repoPedido.existsByCliente(idCliente), "pedidos", deps);
        DependencyUtils.addIf(repoPago.existsByCliente(idCliente), "pagos", deps);
        return deps;
    }
}

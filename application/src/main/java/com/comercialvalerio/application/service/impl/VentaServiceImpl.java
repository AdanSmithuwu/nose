package com.comercialvalerio.application.service.impl;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.application.cache.EstadoCache;
import com.comercialvalerio.application.dto.MotivoDto;
import com.comercialvalerio.application.dto.VentaCreateDto;
import com.comercialvalerio.application.dto.VentaDto;
import com.comercialvalerio.application.mapper.VentaDtoMapper;
import com.comercialvalerio.application.mapper.VentaMapper;
import com.comercialvalerio.application.service.VentaService;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.model.Estado;
import com.comercialvalerio.domain.model.EstadoNombre;
import com.comercialvalerio.domain.model.Venta;
import com.comercialvalerio.domain.repository.VentaRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class VentaServiceImpl implements VentaService {
@Inject
    VentaDtoMapper mapper;

    private final VentaRepository repoVen;
    private final EstadoCache      estadoCache;
    @Inject VentaMapper ventaMapper;

    @Inject
    public VentaServiceImpl(VentaRepository repoVen,
                            EstadoCache estadoCache) {
        this.repoVen = repoVen;
        this.estadoCache = estadoCache;
    }

    @Override
    public List<VentaDto> listar() {
        return ServiceUtils.mapList(repoVen.findAll(), mapper::toDto);
    }

    @Override
    public List<VentaDto> listarPorRango(LocalDateTime d, LocalDateTime h) {
        return ServiceUtils.mapList(repoVen.findByRangoFecha(d, h), mapper::toDto);
    }

    @Override
    public List<VentaDto> listarPorCliente(Integer idCliente) {
        return ServiceUtils.mapList(repoVen.findByCliente(idCliente), mapper::toDto);
    }

    @Override
    public VentaDto obtener(Integer id) {
        Venta venta = ServiceChecks.requireFound(
                repoVen.findById(id), "Venta no encontrada: " + id);
        return mapper.toDto(venta);
    }

    @Override
    public VentaDto crear(VentaCreateDto dto) {
        Venta v = ventaMapper.toVenta(dto);
        if (v.getEstado() == null) {
            Estado completada = estadoCache.get("Transaccion", EstadoNombre.COMPLETADA);
            v.setEstado(completada);
        }
        // Las reglas definitivas para pagos y totales se aplican en
        // trg_Transaccion_Update y trg_PagoTransaccion_CheckSum.
        // El disparador garantiza que la suma de pagos coincida con el total
        // neto al completar la transacción.
        repoVen.save(v);
        return mapper.toDto(v);
    }

    @Override
    public void cancelar(Integer id, MotivoDto motivo) {
        // 1) Validar existencia
        Venta venta = ServiceChecks.requireFound(
                repoVen.findById(id), "Venta no encontrada: " + id);
        // 2) Validaciones de dominio
        Estado cancelada = estadoCache.get("Transaccion", EstadoNombre.CANCELADA);
        venta.cancelar(cancelada, motivo.motivo());
        // 3) Invocar al repositorio que llama al SP sp_CancelarVenta
        repoVen.cancelar(id, motivo.motivo());
    }
}

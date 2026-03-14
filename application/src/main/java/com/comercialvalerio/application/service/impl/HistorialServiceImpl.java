package com.comercialvalerio.application.service.impl;

import java.util.List;

import com.comercialvalerio.application.dto.HistorialDto;
import com.comercialvalerio.application.service.HistorialService;
import com.comercialvalerio.domain.repository.VentaRepository;
import com.comercialvalerio.domain.repository.HistorialRepository;
import com.comercialvalerio.common.time.DateMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class HistorialServiceImpl implements HistorialService {
    @Inject HistorialRepository repo;
    @Inject VentaRepository ventaRepo;
    @Inject com.comercialvalerio.domain.repository.DetalleTransaccionRepository detalleRepo;

    @Override
    public List<HistorialDto> historialPorCliente(Integer idCliente) {
        return repo.findByCliente(idCliente).stream().map(ent -> {
            boolean esVenta = ventaRepo.existsById(ent.idTransaccion());
            String tipo = esVenta ? "Venta" : "Pedido";
            return new HistorialDto(
                    ent.idTransaccion(),
                    DateMapper.toOffsetDateTime(ent.fecha()),
                    ent.totalNeto(),
                    ent.descuento(),
                    ent.cargo(),
                    ent.estado(),
                    tipo);
        }).toList();
    }

    @Override
    public List<HistorialDto> historialPorCliente(Integer idCliente,
            java.time.LocalDateTime desde, java.time.LocalDateTime hasta,
            Integer idCategoria, Integer idProducto) {
        List<com.comercialvalerio.domain.view.HistorialTransaccionView> base;
        if (desde != null || hasta != null) {
            if (desde == null) {
                desde = java.time.LocalDateTime.of(2000, 1, 1, 0, 0);
            }
            if (hasta == null) {
                hasta = java.time.LocalDateTime.now().plusDays(1);
            }
            base = repo.findByCliente(idCliente, desde, hasta);
        } else {
            base = repo.findByCliente(idCliente);
        }
        if (idCategoria != null || idProducto != null) {
            base = base.stream()
                    .filter(v -> matchProducto(v.idTransaccion(), idCategoria, idProducto))
                    .toList();
        }
        return base.stream().map(ent -> {
            boolean esVenta = ventaRepo.existsById(ent.idTransaccion());
            String tipo = esVenta ? "Venta" : "Pedido";
            return new HistorialDto(
                    ent.idTransaccion(),
                    DateMapper.toOffsetDateTime(ent.fecha()),
                    ent.totalNeto(),
                    ent.descuento(),
                    ent.cargo(),
                    ent.estado(),
                    tipo);
        }).toList();
    }

    private boolean matchProducto(Integer idTx, Integer idCategoria, Integer idProducto) {
        if (idCategoria == null && idProducto == null) {
            return true;
        }
        return detalleRepo.findByTransaccion(idTx).stream().anyMatch(d -> {
            if (idProducto != null && idProducto.equals(d.getProducto().getIdProducto())) {
                return true;
            }
            if (idCategoria != null && d.getProducto().getCategoria() != null &&
                    idCategoria.equals(d.getProducto().getCategoria().getIdCategoria())) {
                return true;
            }
            return false;
        });
    }
}

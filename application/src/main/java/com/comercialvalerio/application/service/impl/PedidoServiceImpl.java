package com.comercialvalerio.application.service.impl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.comercialvalerio.application.cache.EstadoCache;
import com.comercialvalerio.application.dto.MotivoDto;
import com.comercialvalerio.application.dto.OrdenCompraPdfDto;
import com.comercialvalerio.application.dto.PagoCreateDto;
import com.comercialvalerio.application.dto.PedidoCreateDto;
import com.comercialvalerio.application.dto.PedidoDto;
import com.comercialvalerio.application.dto.PedidoPendienteDto;
import com.comercialvalerio.application.dto.TelefonoDto;
import com.comercialvalerio.application.exception.PdfGenerationException;
import com.comercialvalerio.application.mapper.PedidoDtoMapper;
import com.comercialvalerio.application.mapper.PedidoMapper;
import com.comercialvalerio.application.service.ComprobanteService;
import com.comercialvalerio.application.service.PedidoService;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.time.DateMapper;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.model.DetalleTransaccion;
import com.comercialvalerio.domain.model.Empleado;
import com.comercialvalerio.domain.model.Estado;
import com.comercialvalerio.domain.model.EstadoNombre;
import com.comercialvalerio.domain.model.MetodoPago;
import com.comercialvalerio.domain.model.MovimientoInventario;
import com.comercialvalerio.domain.model.OrdenCompra;
import com.comercialvalerio.domain.model.OrdenCompraPdf;
import com.comercialvalerio.domain.model.PagoTransaccion;
import com.comercialvalerio.domain.model.Pedido;
import com.comercialvalerio.domain.model.TipoMovimiento;
import com.comercialvalerio.domain.model.TipoMovimientoNombre;
import com.comercialvalerio.domain.model.TipoPedido;
import com.comercialvalerio.domain.notification.EnviarComprobanteEvent;
import com.comercialvalerio.domain.notification.EventBus;
import com.comercialvalerio.domain.repository.DetalleTransaccionRepository;
import com.comercialvalerio.domain.repository.MetodoPagoRepository;
import com.comercialvalerio.domain.repository.OrdenCompraPdfRepository;
import com.comercialvalerio.domain.repository.OrdenCompraRepository;
import com.comercialvalerio.domain.repository.PedidoRepository;
import com.comercialvalerio.domain.repository.TipoMovimientoRepository;
import com.comercialvalerio.domain.repository.TransaccionRepository;
import com.comercialvalerio.domain.security.RequestContext;
import com.comercialvalerio.domain.service.InventarioService;
import com.comercialvalerio.domain.service.OrdenCompraPdfGenerator;
import com.comercialvalerio.domain.util.PdfFileNames;
import com.comercialvalerio.domain.exception.NotificationException;
import java.util.concurrent.CompletionException;
import com.comercialvalerio.infrastructure.notification.EventBusException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Implementación de {@link PedidoService}.
 */
@ApplicationScoped
@Transactional
public class PedidoServiceImpl implements PedidoService {
@Inject
    PedidoDtoMapper mapper;

    private final PedidoRepository      repoPed;
    private final OrdenCompraRepository repoOrden;
    private final OrdenCompraPdfRepository repoOrdenPdf;
    private final MetodoPagoRepository  repoMp;
    private final EstadoCache           estadoCache;
    private final DetalleTransaccionRepository repoDet;
    private final TipoMovimientoRepository repoTipoMov;
    private final InventarioService inventarioSvc;
    private final ComprobanteService    compSvc;
    private final EventBus eventBus;
    private final OrdenCompraPdfGenerator pdfGenerator;
    private final TransaccionRepository repoTx;
    @Inject PedidoMapper pedidoMapper;

    @Inject
    public PedidoServiceImpl(PedidoRepository repoPed,
                             OrdenCompraRepository repoOrden,
                             OrdenCompraPdfRepository repoOrdenPdf,
                             MetodoPagoRepository repoMp,
                             EstadoCache estadoCache,
                             DetalleTransaccionRepository repoDet,
                             TipoMovimientoRepository repoTipoMov,
                             InventarioService inventarioSvc,
                             ComprobanteService compSvc,
                             EventBus eventBus,
                             OrdenCompraPdfGenerator pdfGenerator,
                             TransaccionRepository repoTx) {
        this.repoPed  = repoPed;
        this.repoOrden = repoOrden;
        this.repoOrdenPdf = repoOrdenPdf;
        this.repoMp   = repoMp;
        this.estadoCache = estadoCache;
        this.repoDet  = repoDet;
        this.repoTipoMov = repoTipoMov;
        this.inventarioSvc = inventarioSvc;
        this.compSvc  = compSvc;
        this.eventBus = eventBus;
        this.pdfGenerator = pdfGenerator;
        this.repoTx = repoTx;
    }

    @Override
    public List<PedidoDto> listar() {
        return ServiceUtils.mapList(repoPed.findAll(), mapper::toDto);
    }

    @Override
    public List<PedidoPendienteDto> listarPendientes() {
        return ServiceUtils.mapList(
                repoPed.findPendientesEntrega(),
                p -> new PedidoPendienteDto(
                        p.getIdTransaccion(),
                        p.getCliente() == null ? null
                                : p.getCliente().getNombres() + " "
                                + p.getCliente().getApellidos(),
                        DateMapper.toOffsetDateTime(p.getFecha())));
    }

    @Override
    public List<PedidoDto> listarPorRango(LocalDateTime d, LocalDateTime h) {
        if (d != null && h != null && h.isBefore(d)) {
            throw new IllegalArgumentException("Rango de fechas inválido");
        }
        return ServiceUtils.mapList(repoPed.findByRangoFecha(d, h), mapper::toDto);
    }

    @Override
    public PedidoDto obtener(Integer id) {
        Pedido p = ServiceChecks.requireFound(
                repoPed.findById(id), "Pedido no encontrado: " + id);
        return mapper.toDto(p);
    }

    @Override
    public PedidoDto crear(PedidoCreateDto dto) throws PdfGenerationException {
        Pedido p = pedidoMapper.toPedido(dto);
        if (p.getEstado() == null) {
            Estado proc = estadoCache.get("Transaccion", EstadoNombre.EN_PROCESO);
            p.setEstado(proc);
        }
        repoPed.save(p);

        for (DetalleTransaccion d : p.getDetalles()) {
            OrdenCompra oc = new OrdenCompra();
            oc.setPedido(p);
            oc.setCliente(p.getCliente());
            oc.setProducto(d.getProducto());
            oc.setCantidad(d.getCantidad());
            repoOrden.save(oc);
        }
        p = ServiceChecks.requireFound(
                repoPed.findById(p.getIdTransaccion()), "Pedido no encontrado");
        p.setDetalles(repoDet.findByTransaccion(p.getIdTransaccion()));

        byte[] pdf;
        try {
            pdf = pdfGenerator.generar(p);
        } catch (com.comercialvalerio.domain.exception.PdfGenerationException e) {
            throw new PdfGenerationException("Error generando orden de compra", e);
        }
        OrdenCompraPdf ocPdf = new OrdenCompraPdf();
        ocPdf.setPedido(p);
        ocPdf.setBytesPdf(pdf);
        ocPdf.setFechaGeneracion(LocalDateTime.now());
        repoOrdenPdf.save(ocPdf);

        return mapper.toDto(p);
    }

    @Override
    public PedidoDto actualizar(Integer id, PedidoCreateDto dto) {
        Pedido p = pedidoMapper.toPedido(dto);
        if (TipoPedido.ESPECIAL.equals(p.getTipoPedido())) {
            for (DetalleTransaccion d : p.getDetalles()) {
                var mayorista = d.getProducto().getPrecioMayorista();
                if (mayorista == null)
                    throw new BusinessRuleViolationException(
                            "Producto sin precio mayorista");
                d.setPrecioUnitario(mayorista);
            }
        }
        p.setIdTransaccion(id);
        repoPed.update(p);
        repoPed.clearContext();

        repoOrden.deleteByPedido(id);
        for (DetalleTransaccion d : p.getDetalles()) {
            OrdenCompra oc = new OrdenCompra();
            oc.setPedido(p);
            oc.setCliente(p.getCliente());
            oc.setProducto(d.getProducto());
            oc.setCantidad(d.getCantidad());
            repoOrden.save(oc);
        }
        p = ServiceChecks.requireFound(
                repoPed.findById(id), "Pedido no encontrado");
        p.setDetalles(repoDet.findByTransaccion(id));

        byte[] pdf;
        try {
            pdf = pdfGenerator.generar(p);
        } catch (com.comercialvalerio.domain.exception.PdfGenerationException e) {
            throw new PdfGenerationException("Error generando orden de compra", e);
        }
        OrdenCompraPdf ocPdf = repoOrdenPdf.findByPedido(id);
        if (ocPdf == null) {
            ocPdf = new OrdenCompraPdf();
            ocPdf.setPedido(p);
        }
        ocPdf.setBytesPdf(pdf);
        ocPdf.setFechaGeneracion(LocalDateTime.now());
        repoOrdenPdf.save(ocPdf);

        return mapper.toDto(
                ServiceChecks.requireFound(repoPed.findById(id),
                        "Pedido no encontrado"));
    }

    @Override
    public void cancelar(Integer id, MotivoDto motivo) {
        Pedido p = ServiceChecks.requireFound(
                repoPed.findById(id), "Pedido no encontrado: " + id);
        Estado cancelada = estadoCache.get("Transaccion", EstadoNombre.CANCELADA);
        p.cancelar(cancelada, motivo.motivo());
        repoPed.cancelar(id, motivo.motivo());
        repoTx.actualizarEstado(id, cancelada.getIdEstado(), motivo.motivo());
        repoPed.clearContext();
    }

    @Override
    public List<String> verificarStockEntrega(Integer idPedido) {
        Pedido p = ServiceChecks.requireFound(
                repoPed.findById(idPedido), "Pedido no encontrado: " + idPedido);
        if (TipoPedido.DOMICILIO.equals(p.getTipoPedido())) {
            return List.of();
        }
        List<DetalleTransaccion> detalles = repoDet.findByTransaccion(idPedido);
        List<String> faltantes = new ArrayList<>();
        for (DetalleTransaccion d : detalles) {
            if (!inventarioSvc.tieneStock(d.getProducto(), d.getTallaStock(), d.getCantidad())) {
                faltantes.add(d.getProducto().getNombre());
            }
        }
        return faltantes;
    }

    @Override
    public void marcarEntregado(Integer id, List<PagoCreateDto> pagosDto)
            throws PdfGenerationException {
        Pedido p = ServiceChecks.requireFound(
                repoPed.findById(id), "Pedido no encontrado: " + id);

        List<DetalleTransaccion> detalles = repoDet.findByTransaccion(id);
        if (!TipoPedido.DOMICILIO.equals(p.getTipoPedido())) {
            List<String> faltantes = new ArrayList<>();
            for (DetalleTransaccion d : detalles) {
                if (!inventarioSvc.tieneStock(d.getProducto(), d.getTallaStock(), d.getCantidad())) {
                    faltantes.add(d.getProducto().getNombre());
                }
            }
            if (!faltantes.isEmpty()) {
                throw new BusinessRuleViolationException(
                        "Stock insuficiente para: " + String.join(", ", faltantes));
            }
        }

        List<PagoTransaccion> pagosDomain = pagosDto.stream().map(pg -> {
            MetodoPago mp = ServiceChecks.requireFound(
                    repoMp.findById(pg.idMetodoPago()),
                    "MétodoPago " + pg.idMetodoPago() + " no existe");
            PagoTransaccion tx = new PagoTransaccion();
            tx.setMetodoPago(mp);
            tx.setMonto(pg.monto());
            return tx;
        }).toList();

        p.marcarEntregado(pagosDomain);
        Estado entregada = estadoCache.get("Transaccion", EstadoNombre.ENTREGADA);
        p.setEstado(entregada);
        p.setFechaHoraEntrega(LocalDateTime.now());
        p.setIdEmpleadoEntrega(RequestContext.idEmpleado());
        Empleado empEnt = new Empleado();
        empEnt.setIdPersona(RequestContext.idEmpleado());

        if (!TipoPedido.DOMICILIO.equals(p.getTipoPedido())) {
            TipoMovimiento tmSalida = ServiceChecks.requireFound(
                    repoTipoMov.findByNombre(TipoMovimientoNombre.SALIDA.getNombre()),
                    "TipoMovimiento SALIDA no existe");
            for (DetalleTransaccion d : detalles) {
                MovimientoInventario mov = MovimientoInventario.crear(
                        d.getProducto(), d.getTallaStock(), tmSalida,
                        d.getCantidad(), "Pedido " + id, empEnt);
                inventarioSvc.registrarMovimiento(mov);
            }
        }

        repoPed.marcarEntregado(id, pagosDomain, p.getFechaHoraEntrega(),
                RequestContext.idEmpleado());
        repoTx.actualizarEstado(id, entregada.getIdEstado(), null);
        // Limpiar el contexto de persistencia para que ComprobanteService vea el
        // estado actualizado. El comprobante se generará desde la interfaz.
        repoPed.clearContext();
    }

    @Override
    public byte[] descargarOrden(Integer idPedido) {
        OrdenCompraPdf oc = repoOrdenPdf.findByPedido(idPedido);
        if (oc == null)
            throw new EntityNotFoundException("Orden no encontrada");
        return oc.getBytesPdf();
    }

    @Override
    public OrdenCompraPdfDto obtenerOrden(Integer idPedido) {
        OrdenCompraPdf oc = repoOrdenPdf.findByPedido(idPedido);
        if (oc == null)
            throw new EntityNotFoundException("Orden no encontrada");
        String nombre = PdfFileNames.ordenCompra(oc);
        return new OrdenCompraPdfDto(nombre, oc.getBytesPdf());
    }

    @Override
    public void enviarOrdenWhatsApp(Integer idPedido, TelefonoDto telefono) {
        OrdenCompraPdfDto oc = obtenerOrden(idPedido);
        try {
            eventBus.publish(new EnviarComprobanteEvent(oc.pdf(), telefono.telefono(), oc.nombreArchivo()))
                    .join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof NotificationException ne) {
                throw ne;
            }
            if (cause instanceof EventBusException ebe) {
                String msg =
                    "No hay listeners registrados. Verifique la configuración de notificaciones.";
                java.util.logging.Logger.getLogger(PedidoServiceImpl.class.getName())
                        .log(java.util.logging.Level.SEVERE, msg, ebe);
                throw new NotificationException(msg, ebe);
            }
            java.util.logging.Logger.getLogger(PedidoServiceImpl.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Error enviando WhatsApp", cause);
            throw new NotificationException("Error enviando WhatsApp", cause);
        }
    }
}

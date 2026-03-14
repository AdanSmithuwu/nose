package com.comercialvalerio.application.service.impl;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.application.dto.ComprobanteDto;
import com.comercialvalerio.application.dto.TelefonoDto;
import com.comercialvalerio.application.dto.ComprobantePdfDto;
import com.comercialvalerio.application.mapper.ComprobanteDtoMapper;
import com.comercialvalerio.application.service.ComprobanteService;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.domain.model.Comprobante;
import com.comercialvalerio.domain.model.DetalleTransaccion;
import com.comercialvalerio.domain.model.PagoTransaccion;
import com.comercialvalerio.domain.model.Transaccion;
import com.comercialvalerio.domain.repository.ComprobanteRepository;
import com.comercialvalerio.domain.repository.DetalleTransaccionRepository;
import com.comercialvalerio.domain.repository.PagoTransaccionRepository;
import com.comercialvalerio.domain.repository.TransaccionRepository;
import com.comercialvalerio.domain.repository.PedidoRepository;
import com.comercialvalerio.domain.service.PdfGenerator;
import com.comercialvalerio.application.exception.PdfGenerationException;
import com.comercialvalerio.domain.notification.EventBus;
import com.comercialvalerio.domain.notification.EnviarComprobanteEvent;
import com.comercialvalerio.domain.util.PdfFileNames;
import com.comercialvalerio.domain.exception.NotificationException;
import com.comercialvalerio.infrastructure.notification.whatsapp.TwilioWhatsAppSender;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class ComprobanteServiceImpl implements ComprobanteService {

    private static final Logger LOG =
        Logger.getLogger(ComprobanteServiceImpl.class.getName());

    private final ComprobanteRepository repoComp;
    private final TransaccionRepository repoTx;
    private final PedidoRepository      repoPed;
    private final DetalleTransaccionRepository repoDet;
    private final PagoTransaccionRepository    repoPago;
    private final EventBus eventBus;
    private final PdfGenerator pdfGenerator;
    private final TwilioWhatsAppSender whatsappSender;
@Inject
    ComprobanteDtoMapper mapper;

    @Inject
    public ComprobanteServiceImpl(ComprobanteRepository repoComp,
                                  TransaccionRepository repoTx,
                                  PedidoRepository repoPed,
                                  DetalleTransaccionRepository repoDet,
                                  PagoTransaccionRepository repoPago,
                                  EventBus eventBus,
                                  PdfGenerator pdfGenerator,
                                  TwilioWhatsAppSender whatsappSender) {
        this.repoComp = repoComp;
        this.repoTx   = repoTx;
        this.repoPed  = repoPed;
        this.repoDet  = repoDet;
        this.repoPago = repoPago;
        this.eventBus = eventBus;
        this.pdfGenerator = pdfGenerator;
        this.whatsappSender = whatsappSender;
    }

    /* ---------- Alta ---------- */
    @Override
    public ComprobanteDto generar(Integer idTx) throws PdfGenerationException {

        if (repoComp.findByTransaccion(idTx).isPresent())
            throw new DuplicateEntityException("La transacción ya tiene comprobante");

        Transaccion tx = repoPed.findById(idTx).orElse(null);
        if (tx == null) {
            tx = ServiceChecks.requireFound(
                    repoTx.findById(idTx), "Transacción inexistente");
        }

        /*
         * Al generar el comprobante se asume que la transacción ya fue
         * cerrada. Otros servicios validan el estado y registran los
         * cambios necesarios antes de invocar este método.
         */

        List<DetalleTransaccion> detalles = repoDet.findByTransaccion(idTx);
        List<PagoTransaccion> pagos    = repoPago.findByTransaccion(idTx);
        byte[] pdfBytes;
        try {
            pdfBytes = pdfGenerator.generar(tx, detalles, pagos);
        } catch (com.comercialvalerio.domain.exception.PdfGenerationException e) {
            throw new PdfGenerationException("Error generando comprobante", e);
        }

        Comprobante c = new Comprobante();
        c.setTransaccion(tx);
        c.setFechaEmision(LocalDateTime.now());
        c.setBytesPdf(pdfBytes);

        repoComp.save(c);
        return mapper.toDto(c);
    }

    /* ---------- Consulta ---------- */
    @Override
    public ComprobanteDto obtenerPorTransaccion(Integer idTx) {
        Comprobante c = ServiceChecks.requireFound(
                repoComp.findByTransaccion(idTx),
                "Sin comprobante para la transacción");
        return mapper.toDto(c);
    }

    @Override
    public byte[] descargarPdf(Integer idTx) {
        ComprobantePdfDto dto = obtenerPdf(idTx);
        return dto.pdf();
    }

    @Override
    public ComprobantePdfDto obtenerPdf(Integer idTx) {
        Comprobante c = ServiceChecks.requireFound(
                repoComp.findByTransaccion(idTx),
                "Sin comprobante para la transacción");
        if (c.getTransaccion() == null) {
            Transaccion tx = repoPed.findById(idTx).orElse(null);
            if (tx == null) {
                tx = ServiceChecks.requireFound(
                        repoTx.findById(idTx), "Transacción inexistente");
            }
            c.setTransaccion(tx);
        }
        String nombre = PdfFileNames.comprobante(c);
        return new ComprobantePdfDto(nombre, c.getBytesPdf());
    }

    @Override
    public void enviarWhatsApp(Integer idTx, TelefonoDto telefono) {
        if (!whatsappSender.isEnabled()) {
            throw new NotificationException(
                    "El servicio de WhatsApp está deshabilitado", null);
        }
        ComprobantePdfDto dto = obtenerPdf(idTx);
        String numero = telefono == null || telefono.telefono() == null
                || telefono.telefono().isBlank()
                ? obtenerTelefonoCliente(idTx)
                : telefono.telefono();
        try {
            eventBus.publish(new EnviarComprobanteEvent(dto.pdf(), numero, dto.nombreArchivo()))
                    .join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof NotificationException ne) {
                throw ne;
            }
            LOG.log(Level.SEVERE, "Error enviando WhatsApp", cause);
            throw new NotificationException("Error enviando WhatsApp", cause);
        }
    }

    /** Obtiene el teléfono del cliente asociado a la transacción. */
    private String obtenerTelefonoCliente(Integer idTx) {
        Transaccion tx = repoPed.findById(idTx).orElse(null);
        if (tx == null) {
            tx = ServiceChecks.requireFound(
                    repoTx.findById(idTx), "Transacción inexistente");
        }
        var cliente = tx.getCliente();
        if (cliente == null || cliente.getTelefono() == null
                || cliente.getTelefono().isBlank()) {
            throw new NotificationException(
                    "El cliente no tiene teléfono registrado", null);
        }
        return cliente.getTelefono();
    }
}

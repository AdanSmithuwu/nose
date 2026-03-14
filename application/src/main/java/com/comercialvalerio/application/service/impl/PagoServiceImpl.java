package com.comercialvalerio.application.service.impl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.comercialvalerio.application.dto.PagoCreateDto;
import com.comercialvalerio.application.dto.PagoDto;
import com.comercialvalerio.application.mapper.PagoDtoMapper;
import com.comercialvalerio.application.service.PagoService;
import com.comercialvalerio.application.service.util.SecurityChecks;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.model.MetodoPago;
import com.comercialvalerio.domain.model.PagoTransaccion;
import com.comercialvalerio.domain.model.Transaccion;
import com.comercialvalerio.domain.model.EstadoNombre;
import com.comercialvalerio.domain.repository.MetodoPagoRepository;
import com.comercialvalerio.domain.repository.PagoTransaccionRepository;
import com.comercialvalerio.domain.repository.TransaccionRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class PagoServiceImpl implements PagoService {

    private static final Set<String> ESTADOS_FINALES =
            Set.of(EstadoNombre.COMPLETADA.getNombre(),
                   EstadoNombre.ENTREGADA.getNombre(),
                   EstadoNombre.CANCELADA.getNombre());

    private final PagoTransaccionRepository repoPago;
    private final MetodoPagoRepository      repoMet;
    private final TransaccionRepository     repoTx;
@Inject
    PagoDtoMapper mapper;

    @Inject
    public PagoServiceImpl(PagoTransaccionRepository repoPago,
                           MetodoPagoRepository repoMet,
                           TransaccionRepository repoTx) {
        this.repoPago = repoPago;
        this.repoMet  = repoMet;
        this.repoTx   = repoTx;
    }

    /* ---------- Lectura ---------- */
    @Override
    public List<PagoDto> listar(Integer idTx) {
        return ServiceUtils.mapList(repoPago.findByTransaccion(idTx), mapper::toDto);
    }

    /* ---------- Escritura ---------- */
    @Override
    public PagoDto registrar(Integer idTx, PagoCreateDto dto) {

        Transaccion tx = ServiceChecks.requireFound(
                repoTx.findById(idTx), "Transacción inexistente");

        /* No se permiten pagos a transacciones cerradas */
        String estadoActual = tx.getEstado().getNombre();
        if (ESTADOS_FINALES.contains(estadoActual))
            throw new BusinessRuleViolationException(
                    "La transacción está cerrada; no admite más pagos");

        MetodoPago mp = ServiceChecks.requireFound(
                repoMet.findById(dto.idMetodoPago()),
                "Método de pago inexistente");

        /* Controlar que no se exceda el total */
        BigDecimal sumPagos = repoPago.findByTransaccion(idTx).stream()
                                      .map(PagoTransaccion::getMonto)
                                      .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal nuevoTotal = sumPagos.add(dto.monto());
        if (nuevoTotal.compareTo(tx.getTotalNeto()) > 0)
            throw new DuplicateEntityException(
                    "El pago supera el total a pagar");

        PagoTransaccion pago = new PagoTransaccion();
        pago.setTransaccion(tx);
        pago.setMetodoPago(mp);
        pago.setMonto(dto.monto());
        repoPago.save(pago);

        /* NOTA :  El cambio de estado a Entregada/Completada
           lo realiza explícitamente el flujo de ‘Entrega’,
           no el servicio de pagos.                        */

        return mapper.toDto(pago);
    }

    @Override
    public void eliminar(Integer idTx, Integer idPago) {
        SecurityChecks.requireAdminRole();
        // opcional: validar que la tx exista o que pago pertenezca a esa tx
        repoPago.delete(idPago);
    }
}

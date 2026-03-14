package com.comercialvalerio.application.notification;

import com.comercialvalerio.application.mapper.AlertaStockDtoMapper;
import com.comercialvalerio.application.dto.AlertaStockDto;
import com.comercialvalerio.application.exception.NotificationException;
import com.comercialvalerio.domain.model.AlertaStock;
import com.comercialvalerio.domain.notification.NotificadorAlertaStock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Adaptador que convierte la alerta del dominio a DTO y delega en
 * {@link AlertaStockNotifier}.
 */
@ApplicationScoped
public class AlertaStockNotifierAdapter implements NotificadorAlertaStock {

    private final AlertaStockNotifier notifier;
    private final AlertaStockDtoMapper mapper;

    @Inject
    public AlertaStockNotifierAdapter(AlertaStockNotifier notifier,
                                       AlertaStockDtoMapper mapper) {
        this.notifier = notifier;
        this.mapper = mapper;
    }

    @Override
    public void notificar(AlertaStock alerta) throws com.comercialvalerio.domain.exception.NotificationException {
        AlertaStockDto dto = mapper.toDto(alerta);
        try {
            notifier.notificar(dto);
        } catch (NotificationException ex) {
            // Envuelve la excepción de la capa application en la versión de dominio
            throw new com.comercialvalerio.domain.exception.NotificationException(ex.getMessage(), ex);
        }
    }
}

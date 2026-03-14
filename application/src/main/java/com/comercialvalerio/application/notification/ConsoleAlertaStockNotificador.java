package com.comercialvalerio.application.notification;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.comercialvalerio.application.dto.AlertaStockDto;
import com.comercialvalerio.application.exception.NotificationException;
import com.comercialvalerio.application.mapper.AlertaStockDtoMapper;
import com.comercialvalerio.domain.model.AlertaStock;
import com.comercialvalerio.domain.notification.EventBus;
import com.comercialvalerio.domain.notification.EventListener;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ConsoleAlertaStockNotificador
        implements AlertaStockNotifier, EventListener<AlertaStock> {

    private static final Logger LOG =
            Logger.getLogger(ConsoleAlertaStockNotificador.class.getName());

    private final EventBus bus;
    private final AlertaStockDtoMapper mapper;

    @Inject
    public ConsoleAlertaStockNotificador(EventBus bus,
                                         AlertaStockDtoMapper mapper) {
        this.bus = bus;
        this.mapper = mapper;
        bus.subscribe(AlertaStock.class, this);
    }

    @PreDestroy
    void unregister() {
        bus.unsubscribe(AlertaStock.class, this);
    }

    @Override
    public void notificar(AlertaStockDto alerta) throws NotificationException {
        try {
            String nombre = alerta.productoNombre();
            Object stock = alerta.stockActual();
            LOG.info(() -> String.format(
                "Alerta de stock - Producto: %s, stock actual: %s",
                nombre, stock));
        } catch (NullPointerException ex) {
            LOG.log(Level.SEVERE, ex, ex::getMessage);
            throw new NotificationException(
                "Error al enviar la notificación", ex);
        }
    }

    @Override
    public void onEvent(AlertaStock event) {
        AlertaStockDto dto = mapper.toDto(event);
        notificar(dto);
    }
}

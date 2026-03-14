package com.comercialvalerio.application.notification;

import com.comercialvalerio.application.dto.AlertaStockDto;
import com.comercialvalerio.application.exception.NotificationException;

/** Notificador de alertas de stock. */
public interface AlertaStockNotifier {
    void notificar(AlertaStockDto alerta) throws NotificationException;
}

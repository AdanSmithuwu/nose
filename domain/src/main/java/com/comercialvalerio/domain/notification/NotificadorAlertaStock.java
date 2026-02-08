package com.comercialvalerio.domain.notification;

import com.comercialvalerio.domain.exception.NotificationException;
import com.comercialvalerio.domain.model.AlertaStock;

public interface NotificadorAlertaStock {
    void notificar(AlertaStock alerta) throws NotificationException;
}

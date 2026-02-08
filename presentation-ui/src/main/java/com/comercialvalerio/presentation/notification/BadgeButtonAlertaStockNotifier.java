package com.comercialvalerio.presentation.notification;

import com.comercialvalerio.application.dto.AlertaStockDto;
import com.comercialvalerio.application.notification.AlertaStockNotifier;
import com.comercialvalerio.application.exception.NotificationException;
import com.comercialvalerio.presentation.ui.base.BadgeButton;
import javax.swing.SwingUtilities;

/** Notificador que incrementa la cuenta de un {@link BadgeButton}. */
public class BadgeButtonAlertaStockNotifier implements AlertaStockNotifier {
    private final BadgeButton button;

    public BadgeButtonAlertaStockNotifier(BadgeButton button) {
        this.button = button;
    }

    @Override
    public void notificar(AlertaStockDto alerta) throws NotificationException {
        SwingUtilities.invokeLater(() -> {
            int c = button.getBadgeCount();
            button.setBadgeCount(c + 1);
        });
    }
}

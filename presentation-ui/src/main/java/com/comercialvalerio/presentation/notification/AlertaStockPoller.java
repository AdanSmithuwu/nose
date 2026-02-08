package com.comercialvalerio.presentation.notification;

import com.comercialvalerio.application.dto.AlertaStockDto;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.ui.base.BadgeButton;
import raven.toast.Notifications;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comprueba periódicamente las alertas pendientes y actualiza el contador.
 */
public class AlertaStockPoller {
    private static final Logger LOG = Logger.getLogger(AlertaStockPoller.class.getName());
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        t.setName("alert-poller");
        return t;
    });
    private final BadgeButton button;
    private final BadgeButtonAlertaStockNotifier notifier;
    private volatile int lastCount;

    public AlertaStockPoller(BadgeButton button) {
        this.button = button;
        this.notifier = new BadgeButtonAlertaStockNotifier(button);
    }

    /** Inicia la comprobación cada 30 segundos. */
    public void start() {
        lastCount = button.getBadgeCount();
        executor.scheduleAtFixedRate(this::check, 0, 30, TimeUnit.SECONDS);
    }

    /** Detiene el sondeo. */
    public void stop() {
        executor.shutdownNow();
    }

    private void check() {
        try {
            List<AlertaStockDto> list = UiContext.alertaStockSvc().listarPendientes();
            int count = list.size();
            if (count > lastCount) {
                for (int i = lastCount; i < count; i++) {
                    try {
                        notifier.notificar(list.get(i));
                        SwingUtilities.invokeLater(() ->
                                Notifications.getInstance()
                                        .show(Notifications.Type.WARNING,
                                              "Producto bajo stock"));
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, "Error notificando alerta", ex);
                        ErrorHandler.handle(ex);
                    }
                }
            }
            lastCount = count;
            SwingUtilities.invokeLater(() -> button.setBadgeCount(count));
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error consultando alertas", ex);
            ErrorHandler.handle(ex);
        }
    }
}

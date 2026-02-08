package com.comercialvalerio.infrastructure.notification;

import com.comercialvalerio.domain.exception.NotificationException;
import com.comercialvalerio.domain.notification.EnviarComprobanteEvent;
import com.comercialvalerio.domain.notification.EventBus;
import com.comercialvalerio.domain.notification.EventListener;
import com.comercialvalerio.domain.notification.NotificadorComprobante;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Listener que sube el PDF a Drive y envía el mensaje de WhatsApp. */
@ApplicationScoped
public class EnviarComprobanteListener implements EventListener<EnviarComprobanteEvent> {

    private static final Logger LOG =
        Logger.getLogger(EnviarComprobanteListener.class.getName());

    @Inject
    private EventBus bus;

    @Inject
    private NotificadorComprobante notificador;

    private final AtomicBoolean registered = new AtomicBoolean();

    @PostConstruct
    void register() {
        if (registered.compareAndSet(false, true)) {
            bus.subscribe(EnviarComprobanteEvent.class, this);
            LOG.info("EnviarComprobanteListener registrado");
        }
    }

    /**
     * Permite registrar manualmente el listener en caso de que no se haya
     * inicializado vía {@code @PostConstruct}.
     */
    public void ensureRegistered() {
        if (registered.compareAndSet(false, true)) {
            bus.subscribe(EnviarComprobanteEvent.class, this);
            LOG.info("EnviarComprobanteListener registrado");
        }
    }

    @PreDestroy
    void unregister() {
        if (registered.compareAndSet(true, false)) {
            bus.unsubscribe(EnviarComprobanteEvent.class, this);
            LOG.info("EnviarComprobanteListener removido");
        }
    }

    @Override
    public void onEvent(EnviarComprobanteEvent event) {
        try {
            java.nio.file.Path dir = Files.createTempDirectory("cv-");
            File tmp = dir.resolve(event.fileName()).toFile();
            Files.write(tmp.toPath(), event.pdfBytes());
            try {
                notificador.notificar(tmp, event.telefono());
            } finally {
                Files.deleteIfExists(tmp.toPath());
                Files.deleteIfExists(dir);
            }
        } catch (NotificationException ex) {
            LOG.log(Level.SEVERE, "Error enviando comprobante", ex);
            throw ex;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error enviando comprobante", ex);
            throw new NotificationException("Error enviando comprobante", ex);
        }
    }
}

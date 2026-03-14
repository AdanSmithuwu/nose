package com.comercialvalerio.application.config;

import com.comercialvalerio.infrastructure.notification.EnviarComprobanteListener;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * Inicializa la capa de notificaciones al arrancar la aplicación.
 *
 * Esto asegura que {@link EnviarComprobanteListener} se registre en
 * {@code SimpleEventBus} antes de que se publiquen eventos.
 */
@ApplicationScoped
public class NotificationStartup {
    @Inject
    EnviarComprobanteListener listener;

    /** Observa el inicio de la aplicación para crear este bean. */
    void onStartup(@Observes @Initialized(ApplicationScoped.class) Object event) {
        listener.ensureRegistered();
    }
}

package com.comercialvalerio.application.config;

import com.comercialvalerio.infrastructure.persistence.PersistenceManager;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/** Inicializa la capa de persistencia al arrancar la aplicación. */
@ApplicationScoped
public class PersistenceStartup {
    @Inject
    PersistenceManager pm;

    /**
     * Observa el inicio de la aplicación para que CDI cree este bean al arranque.
     */
    void onStartup(@Observes @Initialized(ApplicationScoped.class) Object event) {
        // intencionalmente vacío
    }

    /**
     * Verifica la inicialización de la capa de persistencia una vez creado el bean.
     */
    @PostConstruct
    void init() {
        pm.runInternal(em -> null);
    }
}

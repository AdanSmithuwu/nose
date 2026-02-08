package com.comercialvalerio.domain.service;

/** Servicio que gestiona el ciclo de vida de la persistencia. */
public interface PersistenceLifecycleManager {
    /** Cierra los recursos de persistencia de la aplicación. */
    void shutdown();
}

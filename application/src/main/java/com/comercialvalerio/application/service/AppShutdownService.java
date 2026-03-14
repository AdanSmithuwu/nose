package com.comercialvalerio.application.service;

/** Servicio para finalizar la aplicación y liberar recursos. */
public interface AppShutdownService {
    /** Cierra recursos compartidos como JPA y pools de conexiones. */
    void shutdown();
}

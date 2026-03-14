package com.comercialvalerio.application.service.impl;

import com.comercialvalerio.application.service.AppShutdownService;
import com.comercialvalerio.domain.service.PersistenceLifecycleManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Implementación que delega el cierre de JPA en {@link PersistenceLifecycleManager}. */
@ApplicationScoped
public class AppShutdownServiceImpl implements AppShutdownService {
    private final PersistenceLifecycleManager lifecycle;

    @Inject
    public AppShutdownServiceImpl(PersistenceLifecycleManager lifecycle) {
        this.lifecycle = lifecycle;
    }

    @Override
    public void shutdown() {
        lifecycle.shutdown();
    }
}

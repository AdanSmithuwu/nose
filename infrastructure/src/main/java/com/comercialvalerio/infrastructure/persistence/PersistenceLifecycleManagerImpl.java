package com.comercialvalerio.infrastructure.persistence;

import com.comercialvalerio.domain.service.PersistenceLifecycleManager;
import jakarta.enterprise.context.ApplicationScoped;

/** Implementación que delega en {@link PersistenceManager}. */
@ApplicationScoped
public class PersistenceLifecycleManagerImpl implements PersistenceLifecycleManager {

    @Override
    public void shutdown() {
        PersistenceManager.shutdown();
    }
}

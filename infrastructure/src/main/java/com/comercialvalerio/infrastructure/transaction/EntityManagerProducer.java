package com.comercialvalerio.infrastructure.transaction;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;

/**
 * Proveedor del {@link EntityManager} actual administrado por {@link TransactionManager}.
 */
@Dependent
public class EntityManagerProducer {
    @Produces
    public EntityManager get() {
        return TransactionManager.getEntityManager();
    }
}

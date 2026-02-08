package com.comercialvalerio.infrastructure.transaction;

import com.comercialvalerio.infrastructure.persistence.PersistenceManager;
import com.comercialvalerio.domain.security.RequestContext;
import jakarta.persistence.TransactionRequiredException;

import static com.comercialvalerio.infrastructure.transaction.SessionContextQueries.CLEAR_CTX_SQL;
import static com.comercialvalerio.infrastructure.transaction.SessionContextQueries.SET_CTX_SQL;
// Las consultas de SessionContextQueries se definen fuera de las entidades
// porque sólo manipulan variables de sesión en SQL Server y no pertenecen a
// un modelo JPA específico.

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestor de transacciones sencillo ligado al hilo actual.
 * <p>
 * Invocar {@link #begin()} inicia una nueva transacción. Si ya existe otra
 * activa en el hilo actual el método devuelve un manejador {@link Tx}
 * liviano que simplemente participa en la transacción existente. El
 * {@link jakarta.persistence.EntityManager} subyacente se comparte y la
 * transacción se confirma o revierte sólo una vez cuando se cierra el
 * manejador externo.
 */
public final class TransactionManager {
    private static final ThreadLocal<EntityManager> CURRENT = new ThreadLocal<>();
    private static final Logger LOG = Logger.getLogger(TransactionManager.class.getName());

    /** Manejador de transacción devuelto por {@link #begin()}. */
    public static final class Tx implements AutoCloseable {
        private final EntityManager em;
        private final Integer idBefore;
        private final boolean root;
        private boolean commit;

        private Tx(EntityManager em, Integer idBefore, boolean root) {
            this.em = em;
            this.idBefore = idBefore;
            this.root = root;
        }

        /** Marca la transacción para commit. */
        public void commit() { if (root) this.commit = true; }

        /** Marca la transacción para rollback. */
        public void rollback() { if (root) this.commit = false; }

        /**
         * Marca la transacción para rollback registrando la causa indicada.
         * @param cause motivo del rollback
         */
        public void rollback(Throwable cause) {
            if (root) {
                this.commit = false;
                if (cause != null) {
                    LOG.log(Level.WARNING, "Rolling back transaction", cause);
                }
            }
        }

        @Override
        public void close() {
            if (!root) {
                return;
            }
            EntityTransaction tx = em.getTransaction();
            try {
                if (tx != null && tx.isActive()) {
                    // reinicia el session context de SQL Server mientras la transacción está activa
                    clearSessionContext(em, idBefore);
                    if (commit) tx.commit(); else tx.rollback();
                }
            } finally {
                em.close();
                CURRENT.remove();
            }
        }
    }

    public static Tx begin() {
        EntityManager em = CURRENT.get();
        EntityTransaction tx = (em != null) ? em.getTransaction() : null;
        boolean root = true;
        if (tx != null && tx.isActive()) {
            root = false;
        } else {
            if (em == null) {
                em = PersistenceManager.create();
                CURRENT.set(em);
                tx = em.getTransaction();
            }
            if (tx != null && !tx.isActive()) {
                tx.begin();
            }
            applySessionContext(em);
        }
        Integer idBefore = RequestContext.idEmpleado();
        return new Tx(em, idBefore, root);
    }

    public static EntityManager getEntityManager() { return CURRENT.get(); }

    public static boolean isActive() {
        EntityManager em = CURRENT.get();
        EntityTransaction tx = (em != null) ? em.getTransaction() : null;
        return em != null && tx != null && tx.isActive();
    }

    /**
     * Ejecuta el trabajo indicado con un {@link EntityManager} temporal,
     * aplicando y limpiando el session context de SQL Server si no hay
     * transacción activa.
     */
    public static <T> T runWithSession(java.util.function.Function<EntityManager, T> work) {
        if (isActive()) {
            return work.apply(getEntityManager());
        }
        return PersistenceManager.run(em -> {
            applySessionContext(em);
            try {
                return work.apply(em);
            } finally {
                clearSessionContext(em, null);
            }
        });
    }

    private static void applySessionContext(EntityManager em) {
        Integer idEmp = RequestContext.idEmpleado();
        if (idEmp != null) {
            /*
             * Los procedimientos almacenados y triggers consultan
             * SESSION_CONTEXT('idEmpleado') para determinar el usuario en curso.
             * Esta consulta guarda el id del empleado actual en el session
             * context de SQL Server para que esos objetos de base de datos
             * puedan accederlo.
             */
            em.createNativeQuery(SET_CTX_SQL)
              .setParameter(1, idEmp)
              .executeUpdate();
        } else {
            /*
             * Al iniciar una transacción sin identificador de empleado se debe
             * limpiar el session context para evitar que la conexión reuse el
             * valor previo de otra solicitud.
             */
            em.createNativeQuery(CLEAR_CTX_SQL)
              .executeUpdate();
        }
    }

    /* Cuando RequestContext.idEmpleado() es null debemos limpiar el session
       context de SQL Server para que las conexiones en el pool no conserven
       el valor previo. */
    private static void clearSessionContext(EntityManager em, Integer idBefore) {
        Integer idAfter = RequestContext.idEmpleado();
        if (!java.util.Objects.equals(idBefore, idAfter)) {
            try {
                // Asegura que las conexiones del pool no mantengan un idEmpleado antiguo
                em.createNativeQuery(CLEAR_CTX_SQL)
                  .executeUpdate();
            } catch (TransactionRequiredException ex) {
                // La conexión podría ya no estar dentro de una transacción
                // (por ejemplo si un procedimiento almacenado hizo commit).
                // En ese caso el session context se limpiará en la siguiente
                // transacción.
                LOG.log(Level.FINE, "No se pudo limpiar el session context", ex);
            }
        }
    }

    private TransactionManager() {}
}

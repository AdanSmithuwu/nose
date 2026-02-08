package com.comercialvalerio.infrastructure.persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.Locale;

import com.comercialvalerio.domain.security.RequestContext;
import com.comercialvalerio.infrastructure.config.AppConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/* Utilidad estática: gestiona el ciclo de vida de {@link EntityManager} */
@ApplicationScoped
public class PersistenceManager {
    /** Instancia compartida para uso de métodos estáticos. */
    private static volatile PersistenceManager INSTANCE;
    private static final Logger LOG = Logger.getLogger(PersistenceManager.class.getName());

    @Inject
    DataSourceProvider dsProvider;

    private EntityManagerFactory employeeEmf;
    private EntityManagerFactory adminEmf;
    private RuntimeException initError;

    @PostConstruct
    void init() {
        INSTANCE = this;
        try {
            Map<String, Object> base = new HashMap<>();
            base.put("jakarta.persistence.jdbc.url", AppConfig.get("db.url"));
            base.put("jakarta.persistence.jdbc.driver", AppConfig.get("db.driver"));

        Map<String, Object> empProps = new HashMap<>(base);
        empProps.put("jakarta.persistence.nonJtaDataSource", dsProvider.employee());
        employeeEmf = Persistence.createEntityManagerFactory("SistemaGestionValerioPU", empProps);

        Map<String, Object> admProps = new HashMap<>(base);
        admProps.put("jakarta.persistence.nonJtaDataSource", dsProvider.admin());
        adminEmf = Persistence.createEntityManagerFactory("SistemaGestionValerioPU", admProps);
            initError = null;
        } catch (RuntimeException ex) {
            initError = ex;
            LOG.severe("Error inicializando JPA: " + ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }
    }

    private synchronized void ensureInit() {
        if (employeeEmf == null || adminEmf == null) {
            init();
        }
    }
    /* Ejecuta una función con un <em>EntityManager</em> nuevo y lo cierra siempre */
    public static <T> T run(Function<EntityManager, T> work) {
        PersistenceManager pm = getInstance();
        return pm.runInternal(work);
    }

    /** Versión no estática que evita nuevas búsquedas en CDI. */
    public <T> T runInternal(Function<EntityManager, T> work) {
        EntityManager em = createInternal();
        try {
            return work.apply(em);
        } finally {
            em.close();
        }
    }

    /* Crea un EntityManager nuevo sin cerrarlo automáticamente */
    public static EntityManager create() {
        return getInstance().createInternal();
    }

    private EntityManager createInternal() {
        ensureInit();
        if (initError != null) {
            throw new IllegalStateException("Error al inicializar la persistencia", initError);
        }
        if (employeeEmf == null || adminEmf == null) {
            throw new IllegalStateException("PersistenceManager not initialized");
        }
        String rol = RequestContext.rol();
        if (rol != null && rol.toLowerCase(Locale.ROOT).startsWith("admin")) {
            return adminEmf.createEntityManager();
        }
        return employeeEmf.createEntityManager();
    }

    private static PersistenceManager getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        return CDI.current().select(PersistenceManager.class).get();
    }

    public PersistenceManager() {
    }

    /**
     * Cierra el {@link EntityManagerFactory} y la fuente de datos subyacente.
     */
    public static void shutdown() {
        try {
            getInstance().shutdownInternal();
        } catch (IllegalStateException ex) {
            // no hay contenedor CDI activo
        }
    }

    @PreDestroy
    void shutdownInternal() {
        if (employeeEmf != null && employeeEmf.isOpen()) {
            employeeEmf.close();
        }
        if (adminEmf != null && adminEmf.isOpen() && adminEmf != employeeEmf) {
            adminEmf.close();
        }
        employeeEmf = null;
        adminEmf = null;
        dsProvider.closePools();
        INSTANCE = null;
    }
}

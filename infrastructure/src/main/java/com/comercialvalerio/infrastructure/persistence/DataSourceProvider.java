package com.comercialvalerio.infrastructure.persistence;

import com.comercialvalerio.infrastructure.config.AppConfig;
import com.comercialvalerio.common.exception.ConfigException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Locale;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import javax.sql.DataSource;

/** Proveedor sencillo que inicializa un pool de conexiones con HikariCP. */
@ApplicationScoped
public class DataSourceProvider {
    private DataSource employeeDs;
    private DataSource adminDs;

    @PostConstruct
    public void init() {
        HikariDataSource emp = createDs("db.employee.user", "db.employee.password");
        HikariDataSource adm = createDs("db.admin.user", "db.admin.password");
        employeeDs = emp;
        adminDs = adm;
    }

    private synchronized void ensureInit() {
        if (employeeDs == null || adminDs == null) {
            init();
        }
    }

    private HikariDataSource createDs(String userKey, String passKey) {
        HikariConfig config = baseConfig();
        config.setUsername(AppConfig.get(userKey));
        config.setPassword(AppConfig.get(passKey));
        try {
            return new HikariDataSource(config);
        } catch (RuntimeException ex) {
            throw new ConfigException("Base de datos no disponible", ex);
        }
    }

    private HikariConfig baseConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(AppConfig.get("db.url"));
        config.setDriverClassName(AppConfig.get("db.driver"));
        config.setMaximumPoolSize(AppConfig.getInt("db.poolSize"));
        config.setMaxLifetime(AppConfig.getInt("db.maxLifetimeMs"));
        config.setConnectionTimeout(2_000);        // falla rápido si la BD no responde
        // verifica la conexión al iniciar para detectar problemas temprano
        config.setInitializationFailTimeout(config.getConnectionTimeout());
        return config;
    }

    public DataSource employee() {
        ensureInit();
        return employeeDs;
    }

    public DataSource admin() {
        ensureInit();
        return adminDs;
    }

    public DataSource get() {
        return employee();
    }
    /** Devuelve un datasource según el rol. Por defecto usa el de empleado. */
    public DataSource forRole(String role) {
        ensureInit();
        if (role != null && role.toLowerCase(Locale.ROOT).startsWith("admin")) {
            return adminDs;
        }
        return employeeDs;
    }

    /**
     * Cierra el {@link HikariDataSource} subyacente cuando termina la aplicación.
     */
    public void closePools() {
        HikariDataSource empDs = (HikariDataSource) employeeDs;
        if (empDs != null && !empDs.isClosed()) {
            empDs.close();
        }
        if (adminDs != null && adminDs != employeeDs) {
            HikariDataSource admDs = (HikariDataSource) adminDs;
            if (!admDs.isClosed()) {
                admDs.close();
            }
        }
        employeeDs = null;
        adminDs = null;
    }

    @PreDestroy
    void close() {
        closePools();
    }
}

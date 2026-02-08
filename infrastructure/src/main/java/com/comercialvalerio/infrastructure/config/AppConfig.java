package com.comercialvalerio.infrastructure.config;

import java.util.Properties;

import com.comercialvalerio.common.config.ConfigUtils;
import com.comercialvalerio.common.exception.ConfigException;
import java.util.Arrays;

/*
 * Lector sencillo de application.properties con overrides por variables de
 * entorno y system properties.
 */
public final class AppConfig {
    private static final Properties P = ConfigUtils.load("application.properties");

    private AppConfig() {}
    /*
     * Devuelve el valor de key:
     * 1) Si existe variable de entorno (key en mayúsculas con puntos→guiones bajos),
     * 2) Si no, el valor en application.properties.
     */
    public static String get(String key) {
        return ConfigUtils.get(P, key, "application.properties");
    }
    /*
     * Para listas separadas por coma.
     */
    public static String[] getList(String key) {
        String val = get(key);
        return val.split("\\s*,\\s*");
    }

    /** Devuelve el valor de la propiedad como {@code int}. */
    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    /** Devuelve el valor de la propiedad como {@code boolean}. */
    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    /**
     * Devuelve el primer valor disponible de las claves indicadas.
     * Útil para mantener compatibilidad cuando cambian los nombres de
     * las propiedades.
     */
    public static String firstOf(String... keys) {
        for (String k : keys) {
            try {
                return ConfigUtils.get(P, k, "application.properties");
            } catch (ConfigException ex) {
                // intenta con la siguiente clave
            }
        }
        throw new ConfigException("Faltan propiedades " + Arrays.toString(keys));
    }
}

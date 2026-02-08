package com.comercialvalerio.presentation.config;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comercialvalerio.common.exception.ConfigException;
import com.comercialvalerio.common.config.ConfigUtils;

public final class UiConfig {
    private static final Properties PROPS = ConfigUtils.load("ui.properties");
    private static final Logger LOG = Logger.getLogger(UiConfig.class.getName());

    private static final int CONNECT_TIMEOUT;
    private static final int READ_TIMEOUT;
    private static final Integer BACKGROUND_POOL_SIZE;
    private static final boolean LOG_BITACORA_RESPONSES;

    static {
        String value = get("client.connectTimeout");
        CONNECT_TIMEOUT = Integer.parseInt(value != null ? value : "5000");

        value = get("client.readTimeout");
        READ_TIMEOUT = Integer.parseInt(value != null ? value : "10000");

        value = optional("ui.backgroundPoolSize");
        Integer size = null;
        if (value != null && !value.isBlank()) {
            try {
                size = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                LOG.log(Level.WARNING,
                        "Invalid value for ui.backgroundPoolSize: " + value, ex);
            }
        }
        BACKGROUND_POOL_SIZE = size;

        value = optional("client.logBitacoraResponses");
        LOG_BITACORA_RESPONSES = value != null && Boolean.parseBoolean(value);
    }

    private static String get(String key) {
        return ConfigUtils.get(PROPS, key, "ui.properties");
    }

    private static String optional(String key) {
        try {
            return ConfigUtils.get(PROPS, key, "ui.properties");
        } catch (ConfigException ex) {
            LOG.log(Level.WARNING, "Failed to read optional property " + key, ex);
            return null;
        }
    }

    public static String getApiBaseUrl() {
        return get("api.baseUrl");
    }

    public static int getConnectTimeout() {
        return CONNECT_TIMEOUT;
    }

    public static int getReadTimeout() {
        return READ_TIMEOUT;
    }

    /** Devuelve el tamaño configurado para los ejecutores en segundo plano o {@code null}. */
    public static Integer getBackgroundPoolSize() {
        return BACKGROUND_POOL_SIZE;
    }

    /** Indica si debe registrarse el cuerpo de las respuestas de bitácoras. */
    public static boolean isLogBitacoraResponses() {
        return LOG_BITACORA_RESPONSES;
    }

    private UiConfig() {}
}

package com.comercialvalerio.domain.config;

/**
 * Proveedor de configuraci\u00f3n para las distintas capas.
 */
public interface ConfigProvider {
    /** Devuelve el valor de la propiedad como {@code boolean}. */
    boolean getBoolean(String key);
}

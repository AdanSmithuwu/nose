package com.comercialvalerio.infrastructure.config;

import com.comercialvalerio.domain.config.ConfigProvider;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementaci\u00f3n de {@link ConfigProvider} basada en {@link AppConfig}.
 */
@ApplicationScoped
public class ConfigProviderImpl implements ConfigProvider {
    @Override
    public boolean getBoolean(String key) {
        return AppConfig.getBoolean(key);
    }
}

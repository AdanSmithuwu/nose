package com.comercialvalerio.common.json;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Provee una instancia compartida de {@link Jsonb} para todos los módulos.
 */
@Provider
@Priority(Priorities.USER)
public class JsonbResolver implements ContextResolver<Jsonb> {
    private static final Logger LOG = Logger.getLogger(JsonbResolver.class.getName());
    private static final Jsonb JSONB;

    static {
        JsonbConfig cfg = new JsonbConfig()
                .withSerializers(new LocalDateTimeSerializer(),
                                 new OffsetDateTimeSerializer())
                .withDeserializers(new LocalDateTimeDeserializer())
                .withAdapters(new LocalDateAdapter(),
                              new OffsetDateTimeAdapter());
        JSONB = JsonbBuilder.create(cfg);
    }

    @Override
    public Jsonb getContext(Class<?> type) {
        return JSONB;
    }

    /** Devuelve la instancia compartida de {@link Jsonb}. */
    public static Jsonb jsonb() {
        return JSONB;
    }

    /** Cierra la instancia compartida de Jsonb cuando la aplicación se detiene. */
    @PreDestroy
    void cleanup() {
        try {
            JSONB.close();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Failed to close Jsonb", ex);
        }
    }
}

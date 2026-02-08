package com.comercialvalerio.presentation.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.comercialvalerio.presentation.config.UiConfig;

/**
 * Proporciona un ejecutor compartido para tareas en segundo plano en todo el módulo de UI.
 */
public final class BackgroundExecutors {
    private static final ExecutorService EXECUTOR;

    static {
        Integer size = UiConfig.getBackgroundPoolSize();
        int threads = size != null && size > 0
                ? size
                : Math.max(2, Runtime.getRuntime().availableProcessors());
        // Nombrar los hilos para facilitar el diagnóstico
        AtomicInteger counter = new AtomicInteger(1);
        ThreadFactory factory = r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setName(String.format("ui-worker-%d", counter.getAndIncrement()));
            return t;
        };
        EXECUTOR = Executors.newFixedThreadPool(threads, factory);
    }

    private BackgroundExecutors() {}

    /**
     * Devuelve el ejecutor usado para las operaciones en segundo plano.
     */
    public static ExecutorService executor() {
        return EXECUTOR;
    }

    /**
     * Detiene el ejecutor compartido y espera brevemente a que las tareas
     * finalicen. Si no terminan en cinco segundos se fuerza su cierre.
     */
    public static void shutdown() {
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException ex) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
            ErrorHandler.handle(ex);
        }
    }

}

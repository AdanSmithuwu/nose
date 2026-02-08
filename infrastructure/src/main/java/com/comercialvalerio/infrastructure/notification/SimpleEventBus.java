package com.comercialvalerio.infrastructure.notification;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.comercialvalerio.domain.notification.EventBus;
import com.comercialvalerio.domain.notification.EventListener;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PreDestroy;

/**
 * Bus de eventos asíncrono sencillo respaldado por un executor.
 * <p>
 * Los listeners se almacenan por clase de evento, incluyendo los
 * registrados para supertipos, por lo que publicar sólo realiza una
 * búsqueda simple.
 */
@ApplicationScoped
public class SimpleEventBus implements EventBus {
    private static final Logger LOG = Logger.getLogger(SimpleEventBus.class.getName());
    /** Segundos de espera para finalizar tareas al cerrar. */
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;
    private final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<EventListener<?>>> listeners =
            new ConcurrentHashMap<>();
    /**
     * Cachea los listeners de una clase de evento, incluidos los registrados
     * para sus superclases e interfaces.
     */
    private final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<EventListener<?>>> dispatchCache =
            new ConcurrentHashMap<>();
    /** Mantiene las entradas del cache afectadas por cada tipo de listener. */
    private final ConcurrentHashMap<Class<?>, ConcurrentHashMap.KeySetView<Class<?>, Boolean>> cacheDependents =
            new ConcurrentHashMap<>();
    /** Relaciona cada tipo de evento cacheado con los tipos que lo componen. */
    private final ConcurrentHashMap<Class<?>, ConcurrentHashMap.KeySetView<Class<?>, Boolean>> cacheDependencies =
            new ConcurrentHashMap<>();
    /** Executor utilizado para despachar eventos de forma asíncrona. */
    private final ExecutorService executor;
    /** Cachea la jerarquía de cada tipo de evento. */
    private static final ConcurrentHashMap<Class<?>, java.util.Set<Class<?>>> supertypeCache =
            new ConcurrentHashMap<>();

    public SimpleEventBus() {
        this(loadPoolSizeFromConfig());
    }

    /**
     * Crea un bus de eventos con el tamaño de pool indicado.
     * El valor cero usa el número de procesadores disponibles. Valores
     * negativos son rechazados.
     */
    public SimpleEventBus(int poolSize) {
        if (poolSize < 0) {
            throw new IllegalArgumentException("Negative poolSize: " + poolSize);
        }
        if (poolSize == 0) {
            LOG.log(Level.WARNING, "Invalid poolSize: {0}", poolSize);
            poolSize = Runtime.getRuntime().availableProcessors();
        }
        AtomicInteger counter = new AtomicInteger(1);
        ThreadFactory factory = r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setName(String.format("event-bus-%d", counter.getAndIncrement()));
            return t;
        };
        executor = Executors.newFixedThreadPool(poolSize, factory);
        // Se preinician los hilos para evitar latencia en el primer evento
        ((ThreadPoolExecutor) executor).prestartAllCoreThreads();
    }

    /**
     * Lee {@code eventBus.poolSize} desde la configuración. Si falta o no es
     * numérico se usa el número de procesadores disponibles. Valores negativos
     * lanzan {@link IllegalArgumentException}.
     */
    private static int loadPoolSizeFromConfig() {
        int threads;
        try {
            threads = com.comercialvalerio.infrastructure.config.AppConfig.getInt("eventBus.poolSize");
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Invalid eventBus.poolSize: {0}", e.getMessage());
            return Runtime.getRuntime().availableProcessors();
        }
        if (threads < 0) {
            throw new IllegalArgumentException("Negative eventBus.poolSize: " + threads);
        }
        return threads;
    }

    private static CopyOnWriteArrayList<EventListener<?>> listFor(
            ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<EventListener<?>>> map,
            Class<?> type) {
        return map.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>());
    }

    private static CopyOnWriteArrayList<EventListener<?>> listOrNull(
            ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<EventListener<?>>> map,
            Class<?> type) {
        return map.get(type);
    }

    private static java.util.Set<Class<?>> allSupertypes(Class<?> type) {
        java.util.Set<Class<?>> result = new java.util.HashSet<>();
        java.util.ArrayDeque<Class<?>> pending = new java.util.ArrayDeque<>();
        pending.add(type);
        while (!pending.isEmpty()) {
            Class<?> cls = pending.poll();
            if (cls == null || !result.add(cls)) {
                continue;
            }
            Class<?> sup = cls.getSuperclass();
            if (sup != null) {
                pending.add(sup);
            }
            for (Class<?> i : cls.getInterfaces()) {
                pending.add(i);
            }
        }
        return result;
    }

    private void recomputeCachedForSubtypes(Class<?> type) {
        java.util.Set<Class<?>> dependents;
        synchronized (cacheDependents) {
            var set = cacheDependents.get(type);
            if (set == null) {
                return;
            }
            dependents = java.util.Set.copyOf(set);
        }
        for (Class<?> cls : dependents) {
            dispatchCache.put(cls, computeCombinedRaw(cls));
        }
    }

    private CopyOnWriteArrayList<EventListener<?>> computeCombinedRaw(Class<?> type) {
        CopyOnWriteArrayList<EventListener<?>> combined = new CopyOnWriteArrayList<>();
        java.util.Set<Class<?>> deps = supertypeCache.computeIfAbsent(type, SimpleEventBus::allSupertypes);
        listeners.forEach((cls, list) -> {
            if (cls.isAssignableFrom(type)) {
                combined.addAll(list);
            }
        });
        synchronized (cacheDependents) {
            java.util.Set<Class<?>> old = cacheDependencies.get(type);
            if (old != null) {
                for (Class<?> dep : old) {
                    java.util.Set<Class<?>> dset = cacheDependents.get(dep);
                    if (dset != null) {
                        dset.remove(type);
                        if (dset.isEmpty()) {
                            cacheDependents.remove(dep);
                        }
                    }
                }
            }
            var newDeps = ConcurrentHashMap.<Class<?>>newKeySet();
            newDeps.addAll(deps);
            cacheDependencies.put(type, newDeps);
            for (Class<?> dep : deps) {
                cacheDependents.computeIfAbsent(dep, k -> ConcurrentHashMap.newKeySet()).add(type);
            }
        }
        return combined;
    }

    @Override
    public <T> void subscribe(Class<T> type, EventListener<T> listener) {
        synchronized (dispatchCache) {
            CopyOnWriteArrayList<EventListener<?>> list = listFor(listeners, type);
            list.add(listener);

            dispatchCache.put(type, computeCombinedRaw(type));
            recomputeCachedForSubtypes(type);
        }
    }

    @Override
    public <T> void unsubscribe(Class<T> type, EventListener<T> listener) {
        synchronized (dispatchCache) {
            CopyOnWriteArrayList<EventListener<?>> list = listOrNull(listeners, type);
            if (list == null || !list.remove(listener)) {
                return;
            }
            if (list.isEmpty()) {
                listeners.remove(type);
                dispatchCache.remove(type);
            } else {
                dispatchCache.put(type, computeCombinedRaw(type));
            }
            recomputeCachedForSubtypes(type);
        }
    }

    /**
     * Publica un evento a todos los listeners registrados. Puede ser invocado
     * de forma concurrente desde varios hilos. El {@link CompletableFuture}
     * devuelto se completa cuando todos los listeners terminan de procesar.
     * Si alguno lanza una excepción o no hay listeners para el evento, el
     * futuro se completa excepcionalmente con {@link EventBusException}. El
     * llamador debe manejar el futuro (por ejemplo registrando o mostrando
     * el error) para evitar fallos silenciosos.
     */
    @Override
    public <T> CompletableFuture<Void> publish(T event) {
        CopyOnWriteArrayList<EventListener<?>> matching = dispatchCache.computeIfAbsent(
            event.getClass(), this::computeCombinedRaw);

        if (matching.isEmpty()) {
            String type = event.getClass().getName();
            LOG.log(Level.WARNING, "Evento {0} publicado sin listeners", type);
            return CompletableFuture.failedFuture(
                    new EventBusException("No hay listeners registrados para "
                            + type));
        }

        Queue<Throwable> errors = new ConcurrentLinkedQueue<>();

        CompletableFuture<?>[] futures = matching.stream()
            .map(l -> CompletableFuture.runAsync(() -> {
                try {
                    @SuppressWarnings("unchecked")
                    EventListener<T> typed = (EventListener<T>) l;
                    typed.onEvent(event);
                } catch (RuntimeException e) { // Solo manejamos fallos esperados
                    LOG.log(Level.WARNING, "Listener threw exception", e);
                    errors.add(e);
                    throw e; // propaga para que publish() falle
                }
            }, executor))
            .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures)
            .handle((v, t) -> {
                if (!errors.isEmpty()) {
                    EventBusException ex = new EventBusException("Uno o más listeners fallaron");
                    errors.forEach(ex::addSuppressed);
                    throw ex;
                }
                if (t != null) {
                    if (t instanceof CompletionException ce) {
                        throw ce;
                    }
                    throw new CompletionException(t);
                }
                return null;
            });
    }

    /** Cierra el executor cuando la aplicación se detiene. */
    @PreDestroy
    void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

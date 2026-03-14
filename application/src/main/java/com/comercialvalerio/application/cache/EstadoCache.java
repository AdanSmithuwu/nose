package com.comercialvalerio.application.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.comercialvalerio.domain.model.Estado;
import com.comercialvalerio.domain.model.EstadoNombre;
import com.comercialvalerio.domain.repository.EstadoRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Caché sencillo para instancias de {@link Estado} utilizadas con frecuencia.
 */
@ApplicationScoped
public class EstadoCache {
    private final EstadoRepository repo;
    private final ConcurrentMap<String, Estado> byKey = new ConcurrentHashMap<>();

    /**
     * Constructor por defecto requerido por los proxies de CDI.
     */
    protected EstadoCache() {
        this.repo = null;
    }

    @Inject
    public EstadoCache(EstadoRepository repo) {
        this.repo = repo;
    }

    /**
     * Devuelve el {@link Estado} para el módulo y nombre indicados.
     *
     * @throws IllegalStateException si no se encuentra
     */
    public Estado get(String modulo, String nombre) {
        String key = modulo + "|" + nombre;
        return byKey.computeIfAbsent(key, k ->
            repo.findByModuloAndNombre(modulo, nombre)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró el estado " + nombre + " para " + modulo))
        );
    }

    /** Sobrecarga de conveniencia usando {@link EstadoNombre}. */
    public Estado get(String modulo, EstadoNombre nombre) {
        return get(modulo, nombre.getNombre());
    }
}

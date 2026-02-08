package com.comercialvalerio.presentation.client;

/**
 * Ayudante para construir proxies REST de los servicios de aplicación.
 */
public final class ServiceProxies {
    private ServiceProxies() {}

    /**
     * Devuelve un proxy de cliente para la interfaz de servicio indicada.
     * El proxy delega en la clase de recurso JAX-RS especificada.
     */
    public static <S, R> S create(Class<S> serviceInterface, Class<R> resourceClass) {
        return RestClientFactory.create(serviceInterface, resourceClass);
    }
}

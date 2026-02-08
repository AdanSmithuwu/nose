package com.comercialvalerio.presentation.client;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import com.comercialvalerio.application.exception.AuthenticationException;
import com.comercialvalerio.presentation.core.ErrorHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidad para construir proxies de cliente para recursos JAX-RS y adaptarlos
 * a las interfaces de servicio usadas en la capa de UI.
 */
public final class RestClientFactory {
    private static final Logger LOG = Logger.getLogger(RestClientFactory.class.getName());

    private record CacheKey(Class<?> service, Class<?> resource) {}
    private static final Map<CacheKey, Map<Method, Method>> METHOD_CACHE = new ConcurrentHashMap<>();

    private RestClientFactory() {}

    /**
     * Crea un proxy para {@code serviceInterface} delegando las llamadas a la
     * clase de recurso JAX-RS indicada. La clase de recurso debe declarar los
     * mismos métodos que la interfaz de servicio.
     */
    public static <S, R> S create(Class<S> serviceInterface, Class<R> resourceClass) {
        Client client = RestClientManager.client();
        ResteasyWebTarget target = (ResteasyWebTarget) client.target(RestClientManager.baseUrl());
        R resourceProxy = target.proxy(resourceClass);

        CacheKey key = new CacheKey(serviceInterface, resourceClass);
        Map<Method, Method> methodCache = METHOD_CACHE.computeIfAbsent(key, k -> {
            Map<Method, Method> map = new java.util.HashMap<>();
            for (Method ifaceMethod : serviceInterface.getMethods()) {
                try {
                    Method resourceMethod = resourceClass.getMethod(
                        ifaceMethod.getName(), ifaceMethod.getParameterTypes());
                    map.put(ifaceMethod, resourceMethod);
                } catch (NoSuchMethodException ex) {
                    // ignorar métodos faltantes (por ejemplo, los de Object)
                }
            }
            return map;
        });

        InvocationHandler handler = (proxy, method, args) -> {
            Method targetMethod = methodCache.get(method);
            if (targetMethod == null) {
                targetMethod = method;
            }
            String http = resolveHttpMethod(targetMethod);
            String url = buildUrl(target, resourceClass, targetMethod);
            Object result;
            try {
                result = targetMethod.invoke(resourceProxy, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }

            if (!(result instanceof Response r)) {
                return result;
            }

            if (method.getReturnType() == Response.class) {
                return r;
            }
            if (r.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                if (r.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                    String msg = readAuthErrorMessage(r);
                    throw new AuthenticationException(msg);
                }
                // la respuesta será procesada por el llamador; mantener abierta
                throw new WebApplicationException(r);
            }

            try {
                if (r.getStatus() == Response.Status.NO_CONTENT.getStatusCode() || !r.hasEntity()) {
                    if (method.getReturnType() == void.class) {
                        return null;
                    }
                    if (method.getReturnType().isPrimitive()) {
                        if (method.getReturnType() == boolean.class) {
                            return false;
                        }
                        if (method.getReturnType() == char.class) {
                            return '\0';
                        }
                        return 0;
                    }
                    return null;
                }

                if (method.getReturnType() == void.class) {
                    return null;
                }

                Type generic = method.getGenericReturnType();
                if (generic instanceof Class<?> cls) {
                    return r.readEntity(cls);
                } else {
                    GenericType<?> gt = new GenericType<Object>(generic) {};
                    return r.readEntity(gt);
                }
            } finally {
                r.close();
            }
        };
        Object proxyInstance = Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface},
                handler);
        return serviceInterface.cast(proxyInstance);
    }

    private static String readAuthErrorMessage(Response r) {
        String msg = "Credenciales inválidas";
        try {
            r.bufferEntity();
            try {
                java.util.Map<String, String> body =
                    r.readEntity(new GenericType<java.util.Map<String, String>>() {});
                msg = body.getOrDefault("error", msg);
            } catch (jakarta.ws.rs.ProcessingException ignore) {
                String raw = r.readEntity(String.class);
                if (raw != null && !raw.isBlank()) {
                    msg = raw;
                }
            }
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Failed to parse error body", ex);
            ErrorHandler.handle(ex);
        } finally {
            r.close();
        }
        return msg;
    }

    private static String resolveHttpMethod(Method m) {
        for (java.lang.annotation.Annotation a : m.getAnnotations()) {
            HttpMethod hm = a.annotationType().getAnnotation(HttpMethod.class);
            if (hm != null) {
                return hm.value();
            }
        }
        return "";
    }

    private static String buildUrl(ResteasyWebTarget target, Class<?> resource, Method m) {
        StringBuilder url = new StringBuilder(target.getUri().toString());
        Path cls = resource.getAnnotation(Path.class);
        if (cls != null) {
            String p = cls.value();
            if (url.charAt(url.length() - 1) == '/' && p.startsWith("/")) {
                url.setLength(url.length() - 1);
            }
            url.append(p);
        }
        Path methodPath = m.getAnnotation(Path.class);
        if (methodPath != null) {
            String p = methodPath.value();
            if (!p.isEmpty() && url.charAt(url.length() - 1) != '/' && !p.startsWith("/")) {
                url.append('/');
            }
            url.append(p);
        }
        return url.toString();
    }
}

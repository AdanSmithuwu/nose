package com.comercialvalerio.application.rest;

import java.net.URI;

import jakarta.ws.rs.core.Response;

import com.comercialvalerio.application.rest.util.Responses;

/**
 * Clase base para recursos REST que ofrece métodos de respuesta auxiliares.
 */
public abstract class BaseResource {

    /**
     * Delega en {@link Responses#ok(Object)}.
     */
    protected Response ok(Object entity) {
        return Responses.ok(entity);
    }

    /**
     * Delega en {@link Responses#created(URI, Object)}.
     */
    protected Response created(URI location, Object entity) {
        return Responses.created(location, entity);
    }

    /**
     * Delega en {@link Responses#noContent()}.
     */
    protected Response noContent() {
        return Responses.noContent();
    }
}

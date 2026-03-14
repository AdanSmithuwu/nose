package com.comercialvalerio.application.rest.util;

import java.net.URI;

import jakarta.ws.rs.core.Response;

/**
 * Métodos auxiliares para construir respuestas estándar.
 */
public final class Responses {

    private Responses() {
    }

    /**
     * Devuelve una respuesta 200 OK con la entidad indicada.
     *
     * @param entity cuerpo que se incluirá en la respuesta
     * @return la respuesta construida
     */
    public static Response ok(Object entity) {
        return Response.ok(entity).build();
    }

    /**
     * Devuelve una respuesta 201 Created con la entidad indicada.
     *
     * @param location URI del recurso creado
     * @param entity   cuerpo que se incluirá en la respuesta
     * @return la respuesta construida
     */
    public static Response created(URI location, Object entity) {
        return Response.created(location).entity(entity).build();
    }

    /**
     * Devuelve una respuesta 204 No Content.
     *
     * @return la respuesta construida
     */
    public static Response noContent() {
        return Response.noContent().build();
    }
}

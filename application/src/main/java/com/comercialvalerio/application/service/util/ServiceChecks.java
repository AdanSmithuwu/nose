package com.comercialvalerio.application.service.util;

import com.comercialvalerio.domain.exception.EntityNotFoundException;

/** Utilidades comunes de validación para las implementaciones de servicio. */
public final class ServiceChecks {
    private ServiceChecks() {}

    /**
     * Garantiza que la entidad proporcionada no sea {@code null}.
     *
     * @param entity  entidad a validar
     * @param message mensaje para {@link EntityNotFoundException}
     * @return la misma entidad si no es nula
     * @throws EntityNotFoundException si la entidad es {@code null}
     */
    public static <T> T requireFound(T entity, String message) {
        if (entity == null) {
            throw new EntityNotFoundException(message);
        }
        return entity;
    }

    /**
     * Garantiza que el {@code Optional} suministrado contenga un valor.
     *
     * @param entity  entidad opcional a validar
     * @param message mensaje para {@link EntityNotFoundException}
     * @return la entidad contenida
     * @throws EntityNotFoundException si el optional está vacío
     */
    public static <T> T requireFound(java.util.Optional<T> entity, String message) {
        return entity.orElseThrow(() -> new EntityNotFoundException(message));
    }
}

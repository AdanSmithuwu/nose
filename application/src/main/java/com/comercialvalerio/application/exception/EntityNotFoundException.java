package com.comercialvalerio.application.exception;

/** Entidad no encontrada. */
public class EntityNotFoundException extends ApplicationException {
    public EntityNotFoundException(String mensaje) {
        super(mensaje);
    }
}

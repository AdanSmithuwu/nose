package com.comercialvalerio.domain.exception;

/* Lanzada cuando no existe la entidad solicitada */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String mensaje) {
        super(mensaje);
    }
}

package com.comercialvalerio.domain.exception;

/* Lanzada cuando el usuario no tiene permisos suficientes. */
public class AuthorizationException extends RuntimeException {
    public AuthorizationException(String mensaje) {
        super(mensaje);
    }

    public AuthorizationException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

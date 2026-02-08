package com.comercialvalerio.domain.exception;

/** Indica una entrada inválida que viola restricciones de la base de datos. */
public class ConstraintViolationException extends DataAccessException {
    public ConstraintViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.comercialvalerio.domain.exception;

/** Superclase genérica para errores relacionados con la persistencia. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.comercialvalerio.application.exception;

/** Error genérico de acceso a datos. */
public class DataAccessException extends ApplicationException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.comercialvalerio.domain.exception;

/** Error de clave duplicada o violación de unicidad. */
public class DuplicateKeyException extends DataAccessException {
    public DuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}

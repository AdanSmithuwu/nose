package com.comercialvalerio.application.exception;

/** Se lanza cuando falla la autenticación. */
public class AuthenticationException extends ApplicationException {
    private final Long minutosRestantes;

    public AuthenticationException(String message) {
        super(message);
        this.minutosRestantes = null;
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.minutosRestantes = null;
    }

    public AuthenticationException(String message, Long minutosRestantes) {
        super(message);
        this.minutosRestantes = minutosRestantes;
    }

    public Long getMinutosRestantes() {
        return minutosRestantes;
    }
}

package com.comercialvalerio.domain.exception;
/*
 * Lanzada cuando falla la autenticación (usuario/contraseña inválidos).
 */
public class AuthenticationException extends RuntimeException {

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

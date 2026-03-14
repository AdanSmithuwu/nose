package com.comercialvalerio.application.exception;

/** Incumplimiento de regla de negocio. */
public class BusinessRuleViolationException extends ApplicationException {
    public BusinessRuleViolationException(String mensaje) {
        super(mensaje);
    }

    public BusinessRuleViolationException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

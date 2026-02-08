package com.comercialvalerio.domain.exception;

/* Excepción genérica de notificaciones. */
public class NotificationException extends RuntimeException {
    public NotificationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

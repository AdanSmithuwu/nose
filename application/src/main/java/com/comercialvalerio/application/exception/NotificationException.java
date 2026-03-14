package com.comercialvalerio.application.exception;

/** Error al enviar una notificación. */
public class NotificationException extends ApplicationException {
    public NotificationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

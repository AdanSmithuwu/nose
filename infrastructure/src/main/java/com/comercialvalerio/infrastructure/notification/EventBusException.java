package com.comercialvalerio.infrastructure.notification;

/** Se lanza cuando la publicación de un evento falla por errores en los listeners. */
public class EventBusException extends RuntimeException {
    public EventBusException(String message) {
        super(message);
    }
}

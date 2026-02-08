package com.comercialvalerio.domain.notification;

@FunctionalInterface
public interface EventListener<T> {
    void onEvent(T event);
}

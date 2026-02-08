package com.comercialvalerio.domain.notification;

import java.util.concurrent.CompletableFuture;

public interface EventBus {
    <T> void subscribe(Class<T> type, EventListener<T> listener);

    /**
     * Elimina un listener registrado previamente mediante
     * {@link #subscribe(Class, EventListener)}. Si el listener no estaba
     * registrado, este método no tiene efecto.
     *
     * @param type     tipo de evento al que estaba suscrito
     * @param listener instancia del listener a remover
     * @param <T>      tipo de evento
     */
    <T> void unsubscribe(Class<T> type, EventListener<T> listener);

    /**
     * Publica un evento de forma asíncrona a todos los listeners registrados.
     *
     * <p>El {@link CompletableFuture} retornado se completa cuando todos los
     * listeners procesan el evento. Si alguno lanza una excepción, el future se
     * completa excepcionalmente con {@link java.util.concurrent.CompletionException}.
     * El llamador debe manejar el future (p. ej. registrar o mostrar un mensaje)
     * para que los errores no pasen desapercibidos.</p>
     */
    <T> CompletableFuture<Void> publish(T event);
}

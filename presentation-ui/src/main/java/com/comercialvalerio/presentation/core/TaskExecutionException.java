package com.comercialvalerio.presentation.core;

/**
 * Excepción lanzada cuando una tarea en segundo plano falla con una
 * excepción comprobada.
 */
public class TaskExecutionException extends RuntimeException {
    public TaskExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

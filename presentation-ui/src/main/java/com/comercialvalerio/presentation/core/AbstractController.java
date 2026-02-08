package com.comercialvalerio.presentation.core;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

public abstract class AbstractController {
    /* Ejecuta una tarea en segundo plano y reporta excepciones al manejador global. */
    protected <T> void runAsync(BackgroundTask<T> task) {
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return task.run(); }
            @Override protected void done() {
                try { task.finish(get()); }
                catch (InterruptedException | ExecutionException ex) {
                    Throwable t = (ex instanceof ExecutionException && ex.getCause() != null)
                                   ? ex.getCause() : ex;
                    ErrorHandler.handle(t);
                }
            }
        }.execute();
    }
    @FunctionalInterface protected interface BackgroundTask<T> {
        T run() throws Exception;
        default void finish(T result) {}
    }
}

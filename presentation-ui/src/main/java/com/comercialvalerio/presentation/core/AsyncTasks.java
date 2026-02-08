package com.comercialvalerio.presentation.core;

import com.comercialvalerio.presentation.ui.common.BusyOverlay;

import javax.swing.SwingUtilities;
import java.awt.Component;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Proporciona métodos auxiliares para ejecutar tareas en segundo plano usando
 * el ejecutor compartido. Todos los errores se reportan mediante
 * {@link ErrorHandler} a menos que se proporcione un manejador personalizado.
 */
public final class AsyncTasks {
    private static final Logger LOG = Logger.getLogger(AsyncTasks.class.getName());
    private AsyncTasks() {}

    static ExecutorService getExecutor() {
        return BackgroundExecutors.executor();
    }

    /**
     * Crea y envía una tarea al ejecutor compartido manejando posibles
     * rechazos.
     */
    private static <T> CompletableFuture<T> submitAsync(Supplier<T> supplier,
                                                        Consumer<Throwable> errCallback) {
        try {
            return CompletableFuture.supplyAsync(supplier, getExecutor());
        } catch (RejectedExecutionException ex) {
            reportError(errCallback, ex);
            CompletableFuture<T> failed = new CompletableFuture<>();
            failed.completeExceptionally(ex);
            return failed;
        }
    }

    private static <T> CompletableFuture<T> executeAsync(Callable<T> task,
                                                         Consumer<T> callback,
                                                         Consumer<Throwable> errCallback) {
        CompletableFuture<T> fut = submitAsync(() -> callTask(task), errCallback);
        return applyCallbacks(fut, callback, errCallback);
    }

    /**
     * Ejecuta {@code task} de forma asíncrona e invoca {@code callback} en el
     * hilo EDT. Cualquier excepción se pasa a {@code errCallback} o se reporta
     * mediante {@link ErrorHandler}.
     */
    public static <T> CompletableFuture<T> run(Callable<T> task,
                                               Consumer<T> callback,
                                               Consumer<Throwable> errCallback) {
        return executeAsync(task, callback, errCallback);
    }

    /** Variante práctica sin manejador de error personalizado. */
    public static <T> CompletableFuture<T> run(Callable<T> task, Consumer<T> callback) {
        return run(task, callback, null);
    }

    /**
     * Ejecuta {@code task} mostrando una superposición de espera sobre
     * {@code view}. El resultado se pasa a {@code callback} en el EDT. Cualquier
     * excepción lanzada se pasa a {@code errCallback} o se reporta mediante
     * {@link ErrorHandler}.
     */
    public static <T> CompletableFuture<T> busy(Component view,
                                                Callable<T> task,
                                                Consumer<T> callback,
                                                Consumer<Throwable> errCallback) {
        return executeAsync(() -> {
            try (BusyOverlay.Handle ov = BusyOverlay.showing(view)) {
                return task.call();
            }
        }, callback, errCallback);
    }

    /** Variante práctica sin manejador de error personalizado. */
    public static <T> CompletableFuture<T> busy(Component view,
                                                Callable<T> task,
                                                Consumer<T> callback) {
        return busy(view, task, callback, null);
    }

    private static <T> T callTask(Callable<T> task) {
        try {
            return task.call();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CompletionException(ex);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new CompletionException(new TaskExecutionException("Error ejecutando tarea", ex));
        }
    }

    private static <T> CompletableFuture<T> applyCallbacks(CompletableFuture<T> future,
                                                           Consumer<T> callback,
                                                           Consumer<Throwable> errCallback) {
        Executor edt = SwingUtilities::invokeLater;
        return future.whenCompleteAsync((result, throwable) -> {
            if (throwable != null) {
                Throwable cause = throwable instanceof CompletionException ce && ce.getCause() != null
                        ? ce.getCause() : throwable;
                reportError(errCallback, cause);
            } else if (callback != null) {
                try {
                    callback.accept(result);
                } catch (RuntimeException ex) {
                    LOG.log(Level.SEVERE,
                            String.format("Callback failed with %s: %s",
                                    ex.getClass().getName(), ex.getMessage()), ex);
                    ErrorHandler.handle(ex);
                    throw ex;
                }
            }
        }, edt);
    }

    private static void reportError(Consumer<Throwable> errCallback, Throwable ex) {
        if (errCallback != null) {
            Runnable task = () -> {
                try {
                    errCallback.accept(ex);
                } catch (RuntimeException cbEx) {
                    LOG.log(Level.SEVERE,
                            String.format("Error callback failed with %s: %s",
                                    cbEx.getClass().getName(), cbEx.getMessage()), cbEx);
                    ErrorHandler.handle(cbEx);
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                task.run();
            } else {
                SwingUtilities.invokeLater(task);
            }
        } else {
            ErrorHandler.handle(ex);
        }
    }
}

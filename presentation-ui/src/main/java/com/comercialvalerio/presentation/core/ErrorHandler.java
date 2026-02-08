package com.comercialvalerio.presentation.core;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.util.Map;
import java.util.Collection;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.validation.ConstraintViolationException;
import com.comercialvalerio.application.exception.DataAccessException;
import com.comercialvalerio.application.exception.AuthenticationException;
import com.comercialvalerio.application.exception.BusinessRuleViolationException;
import com.comercialvalerio.application.exception.EntityNotFoundException;
import com.comercialvalerio.application.exception.NotificationException;
import com.comercialvalerio.common.exception.ConfigException;

public final class ErrorHandler {

    private static final Logger LOG = Logger.getLogger(ErrorHandler.class.getName());

    private static final Pattern TAGS = Pattern.compile("<[^>]*>");

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    public static void handle(Throwable ex) { // registrar en consola
        if (ex instanceof TaskExecutionException tee && tee.getCause() != null) {
            ex = tee.getCause();
        }
        String msg = resolveMessage(ex);
        LOG.log(Level.SEVERE, ex.getMessage(), ex);
        String finalMsg = msg;
        SwingUtilities.invokeLater(() -> {
            Window parent = KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .getActiveWindow();
            JOptionPane.showMessageDialog(parent,
                    finalMsg,
                    "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    public static String resolveMessage(Throwable ex) {
        ex = unwrap(ex);
        String msg = defaultMessage(ex);
        msg = resolveFromWebApplicationException(ex, msg);
        if (msg == null || msg.isBlank()) {
            msg = "Ocurrió un error inesperado";
        }
        return msg;
    }

    private static Throwable unwrap(Throwable ex) {
        if (ex instanceof TaskExecutionException tee && tee.getCause() != null) {
            return tee.getCause();
        }
        if (ex instanceof ExceptionInInitializerError eie && eie.getCause() != null) {
            return eie.getCause();
        }
        if (ex instanceof ExecutionException ee && ee.getCause() != null) {
            return ee.getCause();
        }
        if (ex instanceof CompletionException ce && ce.getCause() != null) {
            return ce.getCause();
        }
        return ex;
    }

    private static String defaultMessage(Throwable ex) {
        if (ex instanceof ProcessingException || ex.getCause() instanceof ProcessingException) {
            return "No se pudo conectar con el servidor";
        }
        if (ex instanceof DataAccessException) {
            String msg = ex.getMessage();
            return (msg == null || msg.isBlank())
                   ? "Error de acceso a datos"
                   : msg;
        }
        if (ex instanceof ConstraintViolationException cve) {
            String msg = cve.getConstraintViolations().stream()
                    .map(v -> v.getMessage())
                    .collect(Collectors.joining("; "));
            return msg.isBlank() ? "Datos inválidos" : msg;
        }
        if (ex instanceof ConfigException) {
            return "Error de configuración de la aplicación";
        }
        if (ex instanceof BusinessRuleViolationException
                || ex instanceof AuthenticationException
                || ex instanceof NotificationException
                || ex instanceof EntityNotFoundException
                || ex instanceof IllegalArgumentException
                || ex instanceof UnsupportedOperationException
                || ex instanceof IllegalStateException) {
            String msg = ex.getMessage();
            if ((msg == null || msg.isBlank()) && ex.getCause() != null) {
                msg = ex.getCause().getMessage();
            }
            return msg;
        }
        return "Ocurrió un error inesperado";
    }

    private static String resolveFromWebApplicationException(Throwable ex, String fallback) {
        WebApplicationException wex = null;
        if (ex instanceof WebApplicationException we) {
            wex = we;
        } else if (ex.getCause() instanceof WebApplicationException we) {
            wex = we;
        }
        if (wex == null) {
            return fallback;
        }
        var resp = wex.getResponse();
        if (resp == null) {
            LOG.log(Level.WARNING,
                    "WebApplicationException with null response");
            return fallback;
        }
        String msg = fallback;
        try {
            resp.bufferEntity();
            var type = resp.getMediaType();
            if (type != null && MediaType.APPLICATION_JSON_TYPE.isCompatible(type)) {
                try {
                    Map<String,Object> body = resp
                            .readEntity(new GenericType<Map<String,Object>>(){});
                    String bodyMsg = null;
                    Object errObj = body.get("error");
                    if (errObj != null) {
                        bodyMsg = convert(errObj);
                    }
                    if (bodyMsg == null || bodyMsg.isBlank()) {
                        Object msgObj = body.get("message");
                        if (msgObj != null) {
                            bodyMsg = convert(msgObj);
                        }
                    }
                    if (bodyMsg == null || bodyMsg.isBlank()) {
                        Object vrObj = body.get("violationReport");
                        if (vrObj instanceof Map<?,?> vr) {
                            var messages = new java.util.ArrayList<String>();
                            for (Object v : vr.values()) {
                                if (v instanceof Collection<?> coll) {
                                    for (Object item : coll) {
                                        if (item instanceof Map<?,?> m && m.get("message") != null) {
                                            String mStr = String.valueOf(m.get("message"));
                                            if (!mStr.isBlank()) {
                                                messages.add(mStr);
                                            }
                                        }
                                    }
                                }
                            }
                            if (!messages.isEmpty()) {
                                bodyMsg = String.join("; ", messages);
                            }
                        }
                    }
                    msg = (bodyMsg == null || bodyMsg.isBlank()) ? fallback : bodyMsg;
                    Object detalleObj = body.get("detalle");
                    String detalle = detalleObj == null
                            ? ""
                            : convert(detalleObj);
                    if (!detalle.isBlank()) {
                        msg = msg + ": " + detalle;
                    }
                    LOG.log(Level.WARNING,
                            "Server returned {0}: {1}",
                            new Object[]{resp.getStatus(), msg});
                } catch (ProcessingException jsonEx) {
                    LOG.log(Level.WARNING, "Failed to parse error response", jsonEx);
                    try {
                        String raw = resp.readEntity(String.class);
                        LOG.log(Level.WARNING, "Raw error: {0}", raw);
                        if (raw != null && !raw.isBlank()) {
                            if (type != null && MediaType.TEXT_HTML_TYPE.isCompatible(type)) {
                                msg = stripHtml(raw);
                            } else {
                                msg = raw;
                            }
                        }
                    } catch (ProcessingException ignore) {
                        LOG.log(Level.WARNING, "Failed to read raw error", ignore);
                    }
                }
            } else {
                try {
                    String raw = resp.readEntity(String.class);
                    LOG.log(Level.WARNING, "Raw error: {0}", raw);
                    if (raw != null && !raw.isBlank()) {
                        if (type != null && MediaType.TEXT_HTML_TYPE.isCompatible(type)) {
                            msg = stripHtml(raw);
                        } else {
                            msg = raw;
                        }
                    }
                } catch (ProcessingException ignore) {
                    LOG.log(Level.WARNING, "Failed to read raw error", ignore);
                }
            }
        } catch (ProcessingException pe) {
            LOG.log(Level.FINE, "Failed to buffer error entity", pe);
        } catch (RuntimeException re) {
            LOG.log(Level.FINE, "Failed to process error response", re);
        } finally {
            resp.close();
        }
        if (msg != null
                && msg.toLowerCase(Locale.ROOT).contains("internal server error")) {
            msg = "Error interno del servidor";
        }
        return msg;
    }

    private static String convert(Object obj) {
        if (obj instanceof Collection<?> c) {
            return c.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("; "));
        }
        if (obj != null && obj.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(obj);
            String[] parts = new String[len];
            for (int i = 0; i < len; i++) {
                Object elem = java.lang.reflect.Array.get(obj, i);
                parts[i] = String.valueOf(elem);
            }
            return String.join("; ", parts);
        }
        return obj == null ? null : String.valueOf(obj);
    }

    private static String stripHtml(String html) {
        if (html == null) {
            return null;
        }
        String text = TAGS.matcher(html).replaceAll(" ");
        return WHITESPACE.matcher(text).replaceAll(" ").trim();
    }
    private ErrorHandler() {}
}

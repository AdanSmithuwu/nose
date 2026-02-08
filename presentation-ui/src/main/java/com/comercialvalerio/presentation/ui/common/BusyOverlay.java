package com.comercialvalerio.presentation.ui.common;

import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;

import com.comercialvalerio.presentation.core.ErrorHandler;

/** Superposición sencilla con barra indeterminada que bloquea la entrada del usuario. */
public class BusyOverlay extends JPanel {
    private final RootPaneContainer owner;
    private final Component previous;
    private final boolean previousVisible;

    private BusyOverlay(RootPaneContainer owner, Component previous, boolean previousVisible) {
        super(new GridBagLayout());
        this.owner = owner;
        this.previous = previous;
        this.previousVisible = previousVisible;
        setOpaque(false);
        // Registrar oyentes vacíos para consumir eventos y evitar que se
        // propaguen a los componentes subyacentes mientras se muestra la
        // barra de progreso.
        addMouseListener(new java.awt.event.MouseAdapter() {});
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {});
        addKeyListener(new java.awt.event.KeyAdapter() {});
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        add(bar);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(0,0,0,100));
        g.fillRect(0,0,getWidth(),getHeight());
        super.paintComponent(g);
    }

    /** Muestra una superposición ocupada sobre la ventana del componente. */
    public static BusyOverlay show(Component c) {
        Window w = SwingUtilities.getWindowAncestor(c);
        if (!(w instanceof RootPaneContainer rpc)) return null;
        JRootPane root = rpc.getRootPane();
        Component old = root.getGlassPane();
        boolean prevVisible = !(old instanceof BusyOverlay) && old.isVisible();
        BusyOverlay ov = new BusyOverlay(rpc, old, prevVisible);
        root.setGlassPane(ov);
        ov.setVisible(true);
        return ov;
    }

    /**
     * Oculta la superposición activa asociada al componente si la hubiera.
     * @param c componente de referencia
     */
    public static void hideFor(Component c) {
        Window w = SwingUtilities.getWindowAncestor(c);
        if (!(w instanceof RootPaneContainer rpc)) return;
        JRootPane root = rpc.getRootPane();
        Component gp = root.getGlassPane();
        if (gp instanceof BusyOverlay ov) {
            ov.hideOverlay();
        }
    }

    /**
     * Oculta la superposición y restaura el glass pane previo solo si este
     * overlay sigue activo. De lo contrario simplemente se descarta para evitar
     * superposiciones eternas.
     */
    public void hideOverlay() {
        JRootPane root = owner.getRootPane();
        if (root.getGlassPane() == this) {
            root.setGlassPane(previous);
            previous.setVisible(previousVisible);
        }
        setVisible(false);
        setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Muestra la superposición de forma asíncrona y devuelve un manejador que
     * la oculta al cerrarse. Pensado para usarse con try-with-resources.
     */
    public static Handle showing(Component view) {
        return new Handle(view);
    }

    /** Manejador de la superposición para usar con try-with-resources. */
    public static final class Handle implements AutoCloseable {
        private final AtomicReference<BusyOverlay> overlay = new AtomicReference<>();

        private Handle(Component view) {
            // Aseguramos que el overlay se muestre antes de comenzar la tarea
            // para evitar que quede visible por una condición de carrera.
            if (SwingUtilities.isEventDispatchThread()) {
                overlay.set(BusyOverlay.show(view));
            } else {
                try {
                    SwingUtilities.invokeAndWait(
                            () -> overlay.set(BusyOverlay.show(view)));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (java.lang.reflect.InvocationTargetException ex) {
                    ErrorHandler.handle(ex);
                    overlay.set(null);
                }
            }
        }

        @Override
        public void close() {
            SwingUtilities.invokeLater(() -> {
                BusyOverlay ov = overlay.get();
                if (ov != null) ov.hideOverlay();
            });
        }
    }
}

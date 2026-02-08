package com.comercialvalerio.presentation.launcher;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.comercialvalerio.presentation.client.RestClientManager;
import com.comercialvalerio.presentation.core.BackgroundExecutors;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.auth.LoginForm;
import com.comercialvalerio.presentation.ui.main.MainForm;
import com.comercialvalerio.presentation.ui.main.dependencies.MenuOption;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.ui.util.FullScreenUtils;
import com.comercialvalerio.presentation.ui.util.UserPrefs;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import jakarta.ws.rs.WebApplicationException;
import raven.toast.Notifications;

/*
 * Clase principal unificada que primero muestra LoginForm en este mismo JFrame
 * y luego cambia a MainForm una vez autenticado el usuario.
 */
public class ValerioApp extends javax.swing.JFrame {

    private static ValerioApp app;
    private static final Logger LOG = Logger.getLogger(ValerioApp.class.getName());
    private final MainForm mainForm;
    private final LoginForm loginForm;

    public ValerioApp() {
        initComponents();

        setTitle("Comercial's Valerio");
        setIconImage(new ImageIcon(getClass()
                .getResource("/com/comercialvalerio/presentation/ui/icon/png/logo.png"))
                .getImage());

        // 1) Configurar tamaño y posición del marco
        Rectangle saved = UserPrefs.getWindowBounds();
        if (saved != null) {
            setBounds(saved);
        } else {
            setSize(new Dimension(1366, 768));
            setLocationRelativeTo(null);
        }

        // 2) Crear instancias de MainForm y LoginForm
        mainForm = new MainForm();
        loginForm = new LoginForm();

        // 3) Iniciar mostrando la pantalla de inicio de sesión
        setContentPane(loginForm);

        // 4) Indicar a FlatLaf que el contenido ocupa todo el marco
        getRootPane().putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);

        // 5) Inicializar el sistema de notificaciones tipo toast
        FullScreenUtils.registerFullScreenShortcut(this);
        if (UserPrefs.isFullScreen()) {
            FullScreenUtils.toggleFullScreen(this);
        }

        // Invocar /logout solo si hay un usuario autenticado
        // al cerrar la ventana sin usar el menú
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
                if (!DialogUtils.confirmAction(ValerioApp.this, "¿Salir de Comercial's Valerio?")) {
                    /* Mantener DO_NOTHING_ON_CLOSE para que la ventana permanezca abierta */
                    return;
                }
                setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
                Rectangle boundsToSave =
                        FullScreenUtils.isFullScreen(ValerioApp.this)
                                && FullScreenUtils.getWindowedBounds(ValerioApp.this) != null
                        ? FullScreenUtils.getWindowedBounds(ValerioApp.this)
                        : getBounds();
                UserPrefs.setWindowBounds(boundsToSave);
                UserPrefs.setFullScreen(FullScreenUtils.isFullScreen(ValerioApp.this));
                if (UiContext.getUsuarioActual() != null) {
                    try {
                        UiContext.empleadoSvc().logout();
                    } catch (WebApplicationException wex) {
                        ErrorHandler.handle(wex);
                    } catch (RuntimeException ex) {
                        ErrorHandler.handle(ex);
                    }
                }
                UiContext.setUsuarioActual(null);
                UiContext.resetProxies();
                app.mainForm.stopAlertPoller();
                app.mainForm.getController().disposeAlertDialog();
                BackgroundExecutors.shutdown();
            }
        });
    }

    /*
     * Método de paso para que MainForm indique qué opción de menú mostrar.
     */
    public static void showForm(Component component) {
        component.applyComponentOrientation(app.getComponentOrientation());
        app.mainForm.showForm(component);
    }

    /*
     * Este método se invoca desde LoginForm al pulsar "Iniciar Sesión".
     * Realiza la animación de transición y reemplaza el contenido
     * actual (loginForm) con el mainForm completo.
     */
    public static void login() {
        FlatAnimatedLafChange.showSnapshot();
        // Cambiar el contenido del marco a mainForm
        app.setContentPane(app.mainForm);
        app.mainForm.applyComponentOrientation(app.getComponentOrientation());
        app.mainForm.refreshUserInfo();
        app.mainForm.applyRole(UiContext.getUsuarioActual().rolNombre());
        // Por defecto seleccionar la opción Dashboard (0,0)
        setSelectedMenu(MenuOption.DASHBOARD.index(), 0);
        // Opcional: ocultar el menú izquierdo inicialmente si se desea
        app.mainForm.hideMenu();
        app.mainForm.startAlertPoller();
        SwingUtilities.updateComponentTreeUI(app.mainForm);
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    /*
     * Este método lo invoca MainForm cuando se presiona "Cerrar Sesión".
     * Reemplaza de nuevo el contenido del marco con loginForm.
     */
    public static void logout() {
        FlatAnimatedLafChange.showSnapshot();
        app.mainForm.stopAlertPoller();
        app.setContentPane(app.loginForm);
        app.loginForm.clearCredentials();
        app.loginForm.syncThemeSwitch();
        app.loginForm.applyComponentOrientation(app.getComponentOrientation());
        SwingUtilities.updateComponentTreeUI(app.loginForm);
        UiContext.setUsuarioActual(null);
        UiContext.resetProxies();
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    /*
     * Permite que MainForm u otro código marque qué menú está seleccionado.
     */
    public static void setSelectedMenu(int index, int subIndex) {
        app.mainForm.setSelectedMenu(index, subIndex);
    }

    // <editor-fold defaultstate="collapsed" desc="Código Generado">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 719, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 521, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    public static void main(String[] args) {
        // Registrar el hook de apagado antes de inicializar para asegurar
        // que los recursos se cierren incluso si init() lanza una excepción.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (RestClientManager.client() != null) {
                try {
                    UiContext.appShutdownSvc().shutdown();
                } catch (RuntimeException ex) {
                    LOG.log(Level.WARNING, "Error invoking shutdown service", ex);
                    ErrorHandler.handle(ex);
                }
            }
            RestClientManager.close();
            BackgroundExecutors.shutdown();
        }));

        /* ─── 1) Inicializar configuración REST / contexto → UiContext ─── */
        try {
            RestClientManager.init();
        } catch (ExceptionInInitializerError | RuntimeException ex) {
            // Si la inicialización falla, detener los hilos de inmediato
            // para evitar que sigan ejecutándose tras System.exit.
            LOG.log(Level.SEVERE, "Failed to initialize UI context", ex);
            ErrorHandler.handle(ex);
            BackgroundExecutors.shutdown();
            System.exit(1);
        }
        // Bloquear la entrada de emojis en cualquier componente de texto
        com.comercialvalerio.presentation.util.EmojiBlocker.installGlobal();

        /* ─── 2) Finalmente, en el EDT, instanciar esta aplicación y mostrarla ─ */
        SwingUtilities.invokeLater(() -> {
            // Antes de crear el frame, cargar fuentes y valores por defecto de FlatLaf
            FlatRobotoFont.install();
            FlatLaf.registerCustomDefaultsSource("com.comercialvalerio.presentation.ui.theme");
            UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
            UIManager.put(
                    "PasswordField.revealIcon",
                    new com.comercialvalerio.presentation.ui.base.PasswordRevealIcon());
            // El color de acento se define por defecto en los archivos de tema
            // de FlatLaf. Se omite cualquier preferencia previa para volver al
            // color naranja original.
            if (UserPrefs.isDarkMode()) {
                FlatMacDarkLaf.setup();
            } else {
                FlatMacLightLaf.setup();
            }
            UIManager.put("OptionPane.yesButtonText", "Sí");
            UIManager.put("OptionPane.noButtonText", "No");

            app = new ValerioApp();
            Notifications.getInstance().setJFrame(app);
            if (!UserPrefs.isFullScreen()) {
                app.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
            }
            app.setVisible(true);
        });
    }
    // Declaración de variables - no modificar//GEN-BEGIN:variables
    // Fin de declaración de variables//GEN-END:variables
}

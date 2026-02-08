package com.comercialvalerio.presentation.ui.auth;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.comercialvalerio.presentation.controller.auth.LoginController;
import com.comercialvalerio.presentation.launcher.ValerioApp;
import com.comercialvalerio.presentation.ui.base.DarkLightSwitchIcon;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.comercialvalerio.presentation.ui.util.UserPrefs;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.BorderFactory;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import net.miginfocom.swing.MigLayout;

/*
 * Formulario de acceso para Comercial's Valerio
 */
public class LoginForm extends BaseForm {

    // Widgets del formulario
    private final JTextField     txtUser  = new JTextField();
    private final JPasswordField txtPass  = new JPasswordField();
    private final JButton        cmdLogin = new JButton("Iniciar Sesión");

    private final DarkLightSwitchIcon switchIcon = new DarkLightSwitchIcon();
    private final JToggleButton  themeSwitch = new JToggleButton(switchIcon);

    // Controlador MVC
    private final LoginController controller = new LoginController(this);

    // Cargamos la imagen de fondo una sola vez
    private final Image bgImage;

    public LoginForm() {
        // Carga la imagen de fondo desde recursos
        ImageIcon ico = new ImageIcon(
            getClass().getResource("/com/comercialvalerio/presentation/ui/bg/loginbg.png")
        );
        bgImage = ico.getImage();
        switchIcon.setIconGap(4);
        switchIcon.setCenterSpace(7);

        buildUI();
        installBehaviours();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // Asignar el botón de inicio de sesión como predeterminado
        // para que pulsar Enter ejecute la autenticación.
        JRootPane rp = SwingUtilities.getRootPane(this);
        if (rp != null) {
            rp.setDefaultButton(cmdLogin);
        }
    }

    /*=====================================================================================
     * Sobrescribimos paintComponent para dibujar la imagen de fondo (escalada a todo el panel)
     *===================================================================================== */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bgImage != null) {
            int panelW = getWidth();
            int panelH = getHeight();

            int imgW = bgImage.getWidth(this);
            int imgH = bgImage.getHeight(this);

            if (imgW > 0 && imgH > 0) {
                // Escalamos cubriendo toda el área sin dejar bordes vacíos
                double scale = Math.max((double) panelW / imgW,
                                        (double) panelH / imgH);
                int scaledW = (int) (imgW * scale);
                int scaledH = (int) (imgH * scale);
                int x = (panelW - scaledW) / 2;
                int y = (panelH - scaledH) / 2;
                g.drawImage(bgImage, x, y, scaledW, scaledH, this);
            }
        }
    }

    /*=====================================================================================
     * Construcción de la UI: tarjeta con degradado, ilustración a la izquierda,
     * formulario a la derecha.
     *===================================================================================== */
    private void buildUI() {
        // Dejamos el panel transparente para que se vea el fondo
        setOpaque(false);
        setLayout(new MigLayout("al center center, inset 0"));
        setBorder(BorderFactory.createEmptyBorder());

        themeSwitch.putClientProperty(FlatClientProperties.STYLE,
                "arc:" + UIStyle.ARC_ROUND + "; innerFocusWidth:0");
        themeSwitch.setSelected(FlatLaf.isLafDark());
        KeyUtils.setTooltipAndMnemonic(themeSwitch, KeyEvent.VK_F6, "Cambiar modo claro/oscuro");
        themeSwitch.setPreferredSize(new Dimension(
                switchIcon.getIconWidth() + 6,
                switchIcon.getIconHeight() + 6));
        themeSwitch.setFocusable(true);
        add(themeSwitch, "pos 10 10");

        // ─── Tarjeta con esquinas redondeadas y degradado ──────────────────────
        JPanel card = new GradientPanel();
        card.setLayout(new MigLayout(
                "wrap 2, gap 50, insets 50 70 50 70",    // margen interno de la tarjeta
                "[grow,fill][400!]",                    // Columna 1 = ilustración (grow), Columna 2 = 400px fijo
                "[]"));                                 // Una sola fila alta suficiente
        add(card, "grow");

         // ─── Ilustración a la izquierda ───────────────────────────────────────
        ImageIcon illustration = new ImageIcon(
            getClass().getResource("/com/comercialvalerio/presentation/ui/icon/png/awa.png")
        );
        JLabel illus = new JLabel(illustration);
        card.add(illus, "w 350!, h 350!");

        // ─── Panel donde van los campos de login (transparentes) ───────────────
        PanelLogin form = new PanelLogin();
        card.add(form, "grow");

        // ─── Componentes internos de PanelLogin ────────────────────────────────
        JLabel lbTitle = new JLabel("Comercial's Valerio");
        lbTitle.putClientProperty(FlatClientProperties.STYLE,
                "font:$h0.font; foreground:rgb(230,230,230)");

        JLabel lbWelcome = new JLabel("¡Bienvenido de nuevo!");
        lbWelcome.putClientProperty(FlatClientProperties.STYLE,
                "font:$h2.font; foreground:rgb(230,230,230)");

        txtUser.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese su usuario");
        txtPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese su contraseña");
        // Revelar/ocultar contraseña y advertir mayúsculas
        txtPass.putClientProperty(FlatClientProperties.STYLE,
                "showRevealButton:true; showCapsLock:true");

        cmdLogin.putClientProperty(FlatClientProperties.STYLE,
                "arc:" + UIStyle.ARC_DIALOG + ";; background:rgb(241, 92, 94); "
              + "foreground:rgb(255,255,255); font:+1");

        KeyUtils.setTooltipAndMnemonic(cmdLogin, KeyEvent.VK_I, "Iniciar Sesión");
        cmdLogin.setEnabled(false);

        // ─── Disposición con MigLayout dentro de PanelLogin ────────────────────
        form.add(lbTitle,          "span, wrap");
        form.add(lbWelcome,        "span, wrap 20");

        JLabel lbUser = new JLabel("Usuario");
        lbUser.putClientProperty(FlatClientProperties.STYLE,
                "foreground:rgb(230,230,230)");
        form.add(lbUser, "wrap");
        form.add(txtUser, "growx, wrap 15");

        JLabel lbPass = new JLabel("Contraseña");
        lbPass.putClientProperty(FlatClientProperties.STYLE,
                "foreground:rgb(230,230,230)");
        form.add(lbPass, "wrap");
        form.add(txtPass, "growx, wrap 5");
        form.add(cmdLogin,                "growx");
        txtPass.setNextFocusableComponent(themeSwitch);
        themeSwitch.setNextFocusableComponent(cmdLogin);
        txtUser.requestFocusInWindow();
    }

    /*=====================================================================================
     * Conecta el botón con la lógica de LoginController
     *===================================================================================== */
    private void installBehaviours() {
        cmdLogin.addActionListener(e -> controller.onLogin());
        themeSwitch.addActionListener(e -> changeMode(themeSwitch.isSelected()));
        javax.swing.event.DocumentListener enabler = new javax.swing.event.DocumentListener() {
            private void update() {
                boolean enable = !txtUser.getText().isBlank()
                        && txtPass.getPassword().length > 0;
                cmdLogin.setEnabled(enable);
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        };
        txtUser.getDocument().addDocumentListener(enabler);
        txtPass.getDocument().addDocumentListener(enabler);
    }

    @Override
    protected void registerShortcuts() {
        KeyUtils.registerFocusAction(this, txtUser);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0),
                () -> themeSwitch.doClick());
    }

    /*=====================================================================================
     * Métodos requeridos por LoginController
     *===================================================================================== */

    /* Devuelve el texto ingresado en “Usuario” (sin espacios al final/inicio). */
    public String getUsuario() {
        return txtUser.getText().trim();
    }

    /* Devuelve la contraseña ingresada (como String). */
    public String getPassword() {
        return new String(txtPass.getPassword());
    }

    /* Limpia únicamente el campo de contraseña. */
    public void clearPassword() {
        txtPass.setText("");
    }

    /* Limpia ambos campos de credenciales. */
    public void clearCredentials() {
        txtUser.setText("");
        txtPass.setText("");
    }

    public JTextField getTxtUser() { return txtUser; }
    public JPasswordField getTxtPass() { return txtPass; }
    public JButton getCmdLogin() { return cmdLogin; }

    /** Sincroniza el botón con el Look and Feel actual. */
    public void syncThemeSwitch() {
        themeSwitch.setSelected(FlatLaf.isLafDark());
    }

    /*
     * Invocado por el controlador cuando la autenticación fue exitosa.
     * Internamente cierra la pantalla de login y llama a la transición del App.
     */
    public void markAuthenticated() {
        // Llamamos a ValerioApp.login() para intercambiar a MainForm
        ValerioApp.login();
    }

    /** Cambia entre modo claro y oscuro del Look and Feel. */
    private void changeMode(boolean dark) {
        if (FlatLaf.isLafDark() != dark) {
            UserPrefs.setDarkMode(dark);
            if (dark) {
                EventQueue.invokeLater(() -> {
                    FlatAnimatedLafChange.showSnapshot();
                    FlatMacDarkLaf.setup();
                    FlatLaf.updateUI();
                    FlatAnimatedLafChange.hideSnapshotWithAnimation();
                });
            } else {
                EventQueue.invokeLater(() -> {
                    FlatAnimatedLafChange.showSnapshot();
                    FlatMacLightLaf.setup();
                    FlatLaf.updateUI();
                    FlatAnimatedLafChange.hideSnapshotWithAnimation();
                });
            }
        }
    }

    /*=====================================================================================
     * Panel interno con degradado y bordes redondeados (20 px).
     *===================================================================================== */
    private static final class GradientPanel extends JPanel {

        GradientPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            float arc = 20f;

            // Degradado vertical de arriba (azul oscuro) a abajo (azul medio)
            LinearGradientPaint lg = new LinearGradientPaint(
                    0, 0, 0, h,
                    new float[]{0f, 1f},
                    new Color[]{ new Color(7, 42, 88), new Color(15, 89, 166) }
            );
            Shape clip = new RoundRectangle2D.Float(0, 0, w, h, arc, arc);

            g2.setPaint(lg);
            g2.fill(clip);
            g2.dispose();
        }
    }
}

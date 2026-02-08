package com.comercialvalerio.presentation.ui.main;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.comercialvalerio.presentation.controller.main.MainController;
import com.comercialvalerio.presentation.ui.main.dependencies.Menu;
import com.comercialvalerio.presentation.ui.main.dependencies.MenuKeyMap;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.UIScale;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.JFrame;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import java.beans.PropertyChangeListener;

import com.comercialvalerio.presentation.ui.base.BadgeButton;
import com.comercialvalerio.presentation.ui.ventas.FormVenta;
import com.comercialvalerio.presentation.ui.pedidos.FormPedidoDomicilio;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.FullScreenUtils;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.ui.core.Refreshable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JLayeredPane;
import javax.swing.border.EmptyBorder;

public class MainForm extends JLayeredPane {

    public MainForm() {
        init();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        UIManager.addPropertyChangeListener(lafListener);
        registerToggleShortcut();
        registerMenuShortcuts();
        registerQuickActions();
        registerRefreshShortcut();
        registerThemeShortcut();
        registerFullScreenShortcut();
    }

    @Override
    public void removeNotify() {
        UIManager.removePropertyChangeListener(lafListener);
        super.removeNotify();
    }

    private void init() {
        // Margen exterior aumentado para no chocar con la barra del sistema
        // Se ajusta en 30 px para elevar la barra superior
        setBorder(new EmptyBorder(30, 5, 5, 5));
        setLayout(new MainFormLayout());

        // -----------------------------------------------------------------
        // 1) Construyo el menú izquierdo (sin cambiar nada aquí)
        // -----------------------------------------------------------------
        menu = new Menu();

        menuButton = new JButton();
        initMenuArrowIcon();
        KeyUtils.setTooltipAndMnemonic(menuButton, KeyEvent.VK_M, "Mostrar/Ocultar menú");
        menuButton.putClientProperty(FlatClientProperties.STYLE,
                "background:$Menu.button.background;arc:" + UIStyle.ARC_ROUND + ";focusWidth:0;borderWidth:0");
        menuButton.addActionListener((ActionEvent e) -> {
            setMenuFull(!menu.isMenuFull());
        });

        // -----------------------------------------------------------------
        // 2) Construyo el panel derecho (rightPanel) que tiene:
        //    - topBar  (estático, nunca cambia)
        //    - contentPanel (lo que cambia al pulsar cada opción del menú)
        // -----------------------------------------------------------------
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        // 2.a) Barra superior (se dibuja permanentemente en la zona NORTE)
        topBar = buildTopBar();
        rightPanel.add(topBar, BorderLayout.NORTH);

        // 2.b) Panel central para “formularios variables” (contentPanel)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        rightPanel.add(contentPanel, BorderLayout.CENTER);

        // -----------------------------------------------------------------
        // 3) Registra eventos de menú delegando al controlador
        // -----------------------------------------------------------------
        initMenuEvent();

        // Finalmente agrego los tres componentes a este JLayeredPane:
        //   1) menuButton (en POPUP_LAYER para flotar encima)
        //   2) menu        (capa normal)
        //   3) rightPanel  (capa normal)
        setLayer(menuButton, JLayeredPane.POPUP_LAYER);
        add(menuButton);
        add(menu);
        add(rightPanel);

        // Carga inicial de alertas pendientes
        refreshAlertas();
    }

    @Override
    public void applyComponentOrientation(ComponentOrientation o) {
        super.applyComponentOrientation(o);
        initMenuArrowIcon(); // si cambia LTR/RTL, ajusta icono de flecha
    }

    // =====================================================================
    //  Construye la “barra superior” que quedará fija en RIGHT NORTH
    // =====================================================================
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(true);
        topBar.setBackground(UIStyle.getColorPanelBg());
        topBar.setBorder(UIStyle.FORM_MARGIN);

        // ── 1. Espaciador en lugar de búsqueda
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(200, 30));
        topBar.add(spacer, BorderLayout.CENTER);

        // ── 2. Botones y avatar a la derecha
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        // “Nuevo Pedido”
        JButton btnPedido = new JButton("Nuevo Pedido");
        btnPedido.putClientProperty(FlatClientProperties.STYLE,
                "arc:" + UIStyle.ARC_ROUND + "; focusWidth:0; borderWidth:0; "
              + "foreground:rgb(15,15,15); background:rgb("
              + UIStyle.COLOR_ACTION_YELLOW.getRed() + ','
              + UIStyle.COLOR_ACTION_YELLOW.getGreen() + ','
              + UIStyle.COLOR_ACTION_YELLOW.getBlue() + ')');
        btnPedido.setIcon(new FlatSVGIcon(
                "com/comercialvalerio/presentation/ui/icon/svg/plus.svg", 0.8f));
        KeyStroke ksPedido = KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK);
        KeyUtils.setTooltipAndMnemonic(btnPedido, ksPedido, "Nuevo Pedido");
        btnPedido.addActionListener(e -> {
            menu.clearSelection();
            showForm(new FormPedidoDomicilio());
        });
        rightBox.add(btnPedido);

        // “Nueva Venta”
        JButton btnVenta = new JButton("Nueva Venta");
        btnVenta.putClientProperty(FlatClientProperties.STYLE,
                "arc:" + UIStyle.ARC_ROUND + "; focusWidth:0; borderWidth:0; "
              + "foreground:rgb(15,15,15); background:rgb("
              + UIStyle.COLOR_ACTION_GREEN.getRed() + ','
              + UIStyle.COLOR_ACTION_GREEN.getGreen() + ','
              + UIStyle.COLOR_ACTION_GREEN.getBlue() + ')');
        btnVenta.setIcon(new FlatSVGIcon(
                "com/comercialvalerio/presentation/ui/icon/svg/plus.svg", 0.8f));
        KeyStroke ksVenta = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK);
        KeyUtils.setTooltipAndMnemonic(btnVenta, ksVenta, "Nueva Venta");
        btnVenta.addActionListener(e -> {
            menu.clearSelection();
            showForm(new FormVenta());
        });
        rightBox.add(btnVenta);

        // Indicador de alertas
        alertaButton = new BadgeButton();
        KeyStroke ksAlert = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
        KeyUtils.setTooltipAndMnemonic(
                alertaButton, "Alertas de stock",
                ksAlert, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK));
        alertaButton.setFocusable(false);
        alertaButton.setIcon(new FlatSVGIcon(
                "com/comercialvalerio/presentation/ui/icon/svg/badge.svg", 1.0f));
        alertaButton.addActionListener(e -> controller.mostrarAlertas());
        rightBox.add(alertaButton);

        // Avatar + Nombre
        JLabel avatar = new JLabel(new ImageIcon(
                getClass().getResource("/com/comercialvalerio/presentation/ui/icon/png/avatar.png")));
        rightBox.add(avatar);

        JPanel userBox = new JPanel(new BorderLayout(2, 0));
        userBox.setOpaque(false);

        lblName = new JLabel();
        lblName.setFont(lblName.getFont().deriveFont(Font.PLAIN, 12f));
        lblRole = new JLabel();
        lblRole.setFont(lblRole.getFont().deriveFont(Font.PLAIN, 10f));

        refreshUserInfo();

        userBox.add(lblName, BorderLayout.NORTH);
        userBox.add(lblRole, BorderLayout.SOUTH);
        rightBox.add(userBox);

        topBar.add(rightBox, BorderLayout.EAST);
        return topBar;
    }

    // =====================================================================
    //  Registra las acciones del menú izquierdo
    //  (cuando el usuario pulsa un ítem, se invoca showForm(panelCorrespondiente))
    // =====================================================================
    private void initMenuEvent() {
        controller.attachMenu(menu);
    }

    // =====================================================================
    //  Invocado por init() para activar/ocultar el menú izquierdo
    // =====================================================================
    private void setMenuFull(boolean full) {
        String icon;
        if (getComponentOrientation().isLeftToRight()) {
            icon = (full) ? "menu_left.svg" : "menu_right.svg";
        } else {
            icon = (full) ? "menu_right.svg" : "menu_left.svg";
        }
        menuButton.setIcon(new FlatSVGIcon("com/comercialvalerio/presentation/ui/icon/svg/" + icon, 0.8f));
        menu.setMenuFull(full);
        revalidate();
    }

    // =====================================================================
    //  Cambia el panel dentro de contentPanel (nueva “página”)
    // =====================================================================
    public void showForm(Component comp) {
        contentPanel.removeAll();
        contentPanel.add(comp, BorderLayout.CENTER);
        contentPanel.repaint();
        contentPanel.revalidate();
    }

    // =====================================================================
    //  Ajusta icono de flecha según LTR/RTL
    // =====================================================================
    private void initMenuArrowIcon() {
        if (menuButton == null) {
            menuButton = new JButton();
        }
        String icon = (getComponentOrientation().isLeftToRight())
                ? "menu_left.svg"
                : "menu_right.svg";
        menuButton.setIcon(new FlatSVGIcon("com/comercialvalerio/presentation/ui/icon/svg/" + icon, 0.8f));
    }

    public void hideMenu() {
        menu.hideMenuItem();
    }

    public void setSelectedMenu(int index, int subIndex) {
        menu.setSelectedMenu(index, subIndex);
    }

    /**
     * Delegar el filtrado del menú según rol en el componente Menu.
     * @param rolNombre nombre del rol
     */
    public void applyRole(String rolNombre) {
        menu.applyRole(rolNombre);
    }

    /** Establece el texto de las etiquetas de usuario en la barra superior. */
    public void updateUserInfo(String nombre, String rol) {
        if (lblName != null) {
            lblName.setText(nombre);
        }
        if (lblRole != null) {
            lblRole.setText(rol);
        }
    }

    /** Actualiza el contador que se muestra en el botón de alertas. */
    public void updateAlertCount(int count) {
        if (alertaButton != null) {
            alertaButton.setBadgeCount(count);
        }
    }

    /** Actualiza la información de usuario mostrada en la barra superior. */
    public void refreshUserInfo() {
        controller.refreshUserInfo();
    }

    /** Refresca el contador de alertas de stock. */
    public void refreshAlertas() {
        controller.refreshAlertas();
    }

    /** Inicia el sondeo automático de alertas. */
    public void startAlertPoller() {
        if (alertaPoller == null && alertaButton != null) {
            alertaPoller = new com.comercialvalerio.presentation.notification.AlertaStockPoller(alertaButton);
            alertaPoller.start();
        }
    }

    /** Detiene el sondeo automático de alertas. */
    public void stopAlertPoller() {
        if (alertaPoller != null) {
            alertaPoller.stop();
            alertaPoller = null;
        }
    }

    /** Devuelve el controlador principal. */
    public MainController getController() {
        return controller;
    }

    /** Registra un atajo de teclado en el root pane para mostrar u ocultar el menú. */
    private void registerToggleShortcut() {
        JRootPane rp = SwingUtilities.getRootPane(this);
        if (rp != null) {
            KeyUtils.registerKeyAction(rp,
                    KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK),
                    () -> setMenuFull(!menu.isMenuFull()));
        }
    }

    /** Registra atajos para cada opción del menú en el root pane. */
    private void registerMenuShortcuts() {
        JRootPane rp = SwingUtilities.getRootPane(this);
        if (rp != null) {
            MenuKeyMap.mapping().forEach((opt, ks) ->
                    KeyUtils.registerKeyAction(rp, ks,
                            () -> menu.setSelectedMenu(opt.index(), 0)));
        }
    }

    /** Registra atajos para los botones de acción rápida en el root pane. */
    private void registerQuickActions() {
        JRootPane rp = SwingUtilities.getRootPane(this);
        if (rp != null) {
            KeyStroke ksPedido = KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK);
            KeyStroke ksVenta = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK);
            KeyStroke ksAlert = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
            KeyUtils.registerKeyAction(rp, ksPedido,
                    () -> {
                        menu.clearSelection();
                        showForm(new FormPedidoDomicilio());
                    });
            KeyUtils.registerKeyAction(rp, ksVenta,
                    () -> {
                        menu.clearSelection();
                        showForm(new FormVenta());
                    });
            KeyUtils.registerKeyAction(rp, ksAlert,
                    controller::mostrarAlertas);
        }
    }

    /** Registra F5 en el root pane para refrescar alertas y el panel actual. */
    private void registerRefreshShortcut() {
        JRootPane rp = SwingUtilities.getRootPane(this);
        if (rp != null) {
            KeyUtils.registerRefreshAction(rp, () -> {
                refreshAlertas();
                if (contentPanel != null && contentPanel.getComponentCount() > 0) {
                    Component comp = contentPanel.getComponent(0);
                    if (comp instanceof Refreshable refreshable) {
                        try {
                            refreshable.refresh();
                        } catch (RuntimeException ex) {
                            ErrorHandler.handle(ex);
                        }
                    }
                }
            });
        }
    }

    /** Registra F6 en el root pane para cambiar el tema de colores. */
    private void registerThemeShortcut() {
        JRootPane rp = SwingUtilities.getRootPane(this);
        if (rp != null) {
            KeyUtils.registerKeyAction(rp,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0),
                    () -> menu.getLightDarkMode()
                            .changeMode(!FlatLaf.isLafDark()));
        }
    }


    /** Registra F11 en el frame para alternar pantalla completa una sola vez. */
    private void registerFullScreenShortcut() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame != null) {
            JRootPane rp = frame.getRootPane();
            Object val = rp.getClientProperty("fullScreenShortcutRegistered");
            if (val == null) {
                FullScreenUtils.registerFullScreenShortcut(frame);
                rp.putClientProperty("fullScreenShortcutRegistered", Boolean.TRUE);
            }
        }
    }

     // =====================================================================
    //  Campos de instancia
    // =====================================================================
    private final MainController controller = new MainController(this);
    private Menu    menu;
    private JButton menuButton;
    private BadgeButton alertaButton;
    private com.comercialvalerio.presentation.notification.AlertaStockPoller alertaPoller;

    // 2) rightPanel es el panel contenedor en la derecha (BorderLayout)
    private JPanel rightPanel;

    // 2.a) Zona NORTE fija (topBar)
    //       – se construye en buildTopBar()
    private JPanel topBar;
    private final PropertyChangeListener lafListener = e -> {
        if ("lookAndFeel".equals(e.getPropertyName()) && topBar != null) {
            topBar.setBackground(UIStyle.getColorPanelBg());
        }
    };

    // 2.b) contentPanel es donde se cargan los formularios cambiantes
    private JPanel contentPanel;

    // Referencias a los labels de usuario en la barra superior
    private JLabel lblName;
    private JLabel lblRole;

    // =====================================================================
    //  Layout personalizado: ubica menú a la izquierda y rightPanel a la derecha
    // =====================================================================
    private class MainFormLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(String name, Component comp) {}

        @Override
        public void removeLayoutComponent(Component comp) {}

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets insets = UIScale.scale(parent.getInsets());
                int gap = UIScale.scale(5);

                Dimension menuSize = menu.getPreferredSize();
                Dimension rightSize = rightPanel.getPreferredSize();

                int width = menuSize.width + gap + rightSize.width;
                int height = Math.max(menuSize.height, rightSize.height);

                width += insets.left + insets.right;
                height += insets.top + insets.bottom;

                return new Dimension(width, height);
            }
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                return new Dimension(0, 0);
            }
        }

        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                boolean ltr = parent.getComponentOrientation().isLeftToRight();
                Insets insets = UIScale.scale(parent.getInsets());
                int x = insets.left;
                int y = insets.top;
                int w = parent.getWidth() - (insets.left + insets.right);
                int h = parent.getHeight() - (insets.top + insets.bottom);

                // ── 1) Ubica menú a la izquierda (ancho dinámico)
                int menuW = UIScale.scale(menu.isMenuFull()
                        ? menu.getMenuMaxWidth()
                        : menu.getMenuMinWidth());
                int menuX = ltr ? x : x + w - menuW;
                menu.setBounds(menuX, y, menuW, h);

                // ── 2) Ubica botón de flecha encima del menú
                int btnW = menuButton.getPreferredSize().width;
                int btnH = menuButton.getPreferredSize().height;
                int btnX = ltr
                        ? (int) (x + menuW - (btnW * (menu.isMenuFull() ? 0.5f : 0.3f)))
                        : (int) (menuX - (btnW * (menu.isMenuFull() ? 0.5f : 0.7f)));
                menuButton.setBounds(btnX, UIScale.scale(55), btnW, btnH);

                // ── 3) Zona DERECHA entera = lo que quede tras restar el menú y un gap
                int gap = UIScale.scale(5);
                int rightX = ltr ? (x + menuW + gap) : x;
                int rightW = w - menuW - gap;
                rightPanel.setBounds(rightX, y, rightW, h);
            }
        }
    }
}

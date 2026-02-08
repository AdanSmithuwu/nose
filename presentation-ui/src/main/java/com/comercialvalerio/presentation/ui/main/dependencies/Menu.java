package com.comercialvalerio.presentation.ui.main.dependencies;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;


import com.comercialvalerio.presentation.ui.main.mode.LightDarkMode;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.application.dto.RolNombre;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;
import java.util.logging.Logger;

/*
 * Menú lateral adaptable (versión Comercial's Valerio).
 *
 * Si deseas modificar los textos o añadir nuevas opciones,
 * edita el contenido del arreglo {@code menuItems}.
 */
public class Menu extends JPanel {

    /* ===================================================================== */
    /*  A R E G L O   C O N   L A S   O P C I O N E S   D E L   M E N Ú       */
    /* ===================================================================== */
   /* ────────────────────────────── Ítems del menú ───────────────────── */
    private final String[][] menuItems = {
        {"Menú Principal"},
        {"~GESTIÓN GENERAL~"},       {"Clientes"},          {"Empleados"},   {"Categorías"},
        {"~GESTIÓN DE TRANSACCIONES~"},{"Pedido Especial"},{"Seguimiento de Ventas"},{"Seguimiento de Pedidos"},
        {"~GESTIÓN DE PRODUCTOS~"},  {"Productos"},         {"Inventario"},  {"Historial de Inventario"},
        {"~REPORTES~"},              {"Reporte Diario"},    {"Reporte Mensual"},{"Reporte de Rotación"},
        {"~CONFIGURACIÓN~"},         {"Bitácora de Accesos"}, {"Parámetros"},
        {"Mantenimiento"},
        /* espacio visual antes de salir */ {"~ ~"},
        {"Cerrar sesión"}
    };

    public boolean isMenuFull() {
        return menuFull;
    }

    public void setMenuFull(boolean menuFull) {
        this.menuFull = menuFull;
        if (menuFull) {
            header.setText(headerName);
            header.setHorizontalAlignment(JLabel.CENTER);
        } else {
            header.setText("");
            header.setHorizontalAlignment(JLabel.CENTER);
        }
        for (Component com : panelMenu.getComponents()) {
            if (com instanceof MenuItem menuItem) {
                menuItem.setFull(menuFull);
            }
        }
        lightDarkMode.setMenuFull(menuFull);
    }

    private final List<MenuEvent> events = new ArrayList<>();
    private boolean menuFull = true;
    private final String headerName =
            "<html><div style='text-align:center'>COMERCIAL'S<br>VALERIO</div></html>";
    private static final Logger LOG = Logger.getLogger(Menu.class.getName());

    protected final boolean hideMenuTitleOnMinimum = true;
    protected final int menuTitleLeftInset = 5;
    protected final int menuTitleVgap = 5;
    protected final int menuMaxWidth = 250;
    protected final int menuMinWidth = 60;
    protected final int headerFullHgap = 5;

    public Menu() {
        init();
    }

    private void init() {
        setLayout(new MenuLayout());
        putClientProperty(FlatClientProperties.STYLE,
                "border:10,2,2,2; background:$Menu.background; arc:" + UIStyle.ARC_DIALOG);

        header = new JLabel(headerName, JLabel.CENTER);
        header.setIcon(new ImageIcon(getClass()
                .getResource("/com/comercialvalerio/presentation/ui/icon/png/logo.png")));
        header.setIconTextGap(UIScale.scale(5));
        header.setHorizontalTextPosition(JLabel.CENTER);
        header.setVerticalTextPosition(JLabel.BOTTOM);
        header.putClientProperty(FlatClientProperties.STYLE,
                "font:$Menu.header.font; foreground:$Menu.foreground");

        scroll = new JScrollPane();
        panelMenu = new JPanel(new MenuItemLayout(this));
        panelMenu.putClientProperty(FlatClientProperties.STYLE,
                "border:5,5,5,5; background:$Menu.background");
        scroll.setViewportView(panelMenu);
        scroll.putClientProperty(FlatClientProperties.STYLE,"border:null");
        JScrollBar vscroll = scroll.getVerticalScrollBar();
        vscroll.setUnitIncrement(10);
        vscroll.putClientProperty(FlatClientProperties.STYLE,
                "width:$Menu.scroll.width; trackInsets:$Menu.scroll.trackInsets; "
              + "thumbInsets:$Menu.scroll.thumbInsets; background:$Menu.ScrollBar.background; "
              + "thumb:$Menu.ScrollBar.thumb");

        createMenu();

        InputMap im = panelMenu.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDown");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveUp");
        ActionMap am = panelMenu.getActionMap();
        am.put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelMenu.transferFocus();
            }
        });
        am.put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelMenu.transferFocusBackward();
            }
        });

        lightDarkMode = new LightDarkMode();

        add(header);
        add(scroll);
        add(lightDarkMode);
    }

    private void createMenu() {
        int index = 0;
        for (String[] item : menuItems) {
            String name = item[0];
            if (name.startsWith("~") && name.endsWith("~")) {
                panelMenu.add(createTitle(name));
            } else {
                MenuItem menuItem = new MenuItem(this, item, index++, events);
                /* separador superior y margen extra para “Cerrar sesión” */
                if ("Cerrar sesión".equals(name)) {
                    int gapTop = UIScale.scale(10);
                    int gapBottom = UIScale.scale(5);
                    menuItem.setBorder(new EmptyBorder(gapTop, 0, gapBottom, 0));
                    logoutItem = menuItem;
                }
                panelMenu.add(menuItem);
            }
        }
    }

    private JLabel createTitle(String text) {
        String t = text.substring(1, text.length()-1).trim();
        JLabel lbl = new JLabel(t.isEmpty() ? " " : t);
        lbl.putClientProperty(FlatClientProperties.STYLE,
                "font:$Menu.label.font; foreground:$Menu.title.foreground");
        return lbl;
    }

    public void setSelectedMenu(int index, int subIndex) {
        runEvent(index, subIndex);
    }

    protected void setSelected(int index, int subIndex) {
        int size = panelMenu.getComponentCount();
        for (int i = 0; i < size; i++) {
            Component com = panelMenu.getComponent(i);
            if (com instanceof MenuItem item) {
                if (item.getMenuIndex() == index) {
                    item.setSelectedIndex(subIndex);
                } else {
                    item.setSelectedIndex(-1);
                }
            }
        }
    }

    protected void runEvent(int index, int subIndex) {
        MenuAction menuAction = new MenuAction();
        for (MenuEvent event : events) {
            event.menuSelected(index, subIndex, menuAction);
        }
        if (!menuAction.isCancel()) {
            setSelected(index, subIndex);
        }
    }

    public void addMenuEvent(MenuEvent event) {
        events.add(event);
    }

    /** Limpia la selección actual del menú. */
    public void clearSelection() {
        setSelected(-1, -1);
    }


    public void hideMenuItem() {
        for (Component com : panelMenu.getComponents()) {
            if (com instanceof MenuItem menuItem) {
                menuItem.hideMenuItem();
            }
        }
        revalidate();
    }

    /**
     * Muestra u oculta las entradas exclusivas de administrador según el rol.
     *
     * @param rolNombre nombre del rol del usuario autenticado
     */
    public void applyRole(String rolNombre) {
        boolean admin = rolNombre != null
                && RolNombre.fromNombre(rolNombre) == RolNombre.ADMINISTRADOR;
        for (Component com : panelMenu.getComponents()) {
            if (com instanceof MenuItem item) {
                MenuOption opt = MenuOption.fromIndex(item.getMenuIndex());
                boolean visible = switch (opt) {
                    case EMPLEADOS, CATEGORIAS, HISTORIAL_INVENTARIO,
                         REPORTE_DIARIO, REPORTE_MENSUAL,
                         REPORTE_ROTACION, PARAMETROS,
                         BITACORA_ACCESOS, MANTENIMIENTO -> admin;
                    default -> true;
                };
                item.setVisible(visible);
            } else if (com instanceof JLabel label) {
                String text = label.getText();
                if ("REPORTES".equalsIgnoreCase(text)
                        || "CONFIGURACIÓN".equalsIgnoreCase(text)) {
                    label.setVisible(admin);
                }
            }
        }
        adjustLogoutGap(admin);
        revalidate();
        repaint();
    }

    private void adjustLogoutGap(boolean admin) {
        if (logoutItem == null) {
            return;
        }
        int gapTop = UIScale.scale(10);
        int gapBottom = UIScale.scale(5);
        if (!admin) {
            int hidden = 0;
            for (Component c : panelMenu.getComponents()) {
                if (!c.isVisible() && c != logoutItem) {
                    hidden += c.getPreferredSize().height;
                    if (c instanceof JLabel) {
                        hidden += UIScale.scale(menuTitleVgap) * 2;
                    }
                }
            }
            gapTop += hidden;
        }
        logoutItem.setBorder(new EmptyBorder(gapTop, 0, gapBottom, 0));
    }

    public boolean isHideMenuTitleOnMinimum() {
        return hideMenuTitleOnMinimum;
    }

    public int getMenuTitleLeftInset() {
        return menuTitleLeftInset;
    }

    public int getMenuTitleVgap() {
        return menuTitleVgap;
    }

    public int getMenuMaxWidth() {
        return menuMaxWidth;
    }

    public int getMenuMinWidth() {
        return menuMinWidth;
    }

    public LightDarkMode getLightDarkMode() {
        return lightDarkMode;
    }

    private JLabel header;
    private JScrollPane scroll;
    private JPanel panelMenu;
    private LightDarkMode lightDarkMode;
    private MenuItem logoutItem;

    private class MenuLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                return new Dimension(5, 5);
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
                Insets insets = parent.getInsets();
                int x = insets.left;
                int y = insets.top;
                int gap = UIScale.scale(5);
                int sheaderFullHgap = UIScale.scale(headerFullHgap);
                int width = parent.getWidth() - (insets.left + insets.right);
                int height = parent.getHeight() - (insets.top + insets.bottom);
                int iconWidth = width;
                int iconHeight = header.getPreferredSize().height;
                int hgap = menuFull ? sheaderFullHgap : 0;
                int accentColorHeight = 0;

                header.setBounds(x + hgap, y, iconWidth - (hgap * 2), iconHeight);
                int ldgap = UIScale.scale(10);
                int ldWidth = width - ldgap * 2;
                int ldHeight = lightDarkMode.getPreferredSize().height;
                int ldx = x + ldgap;
                int ldy = y + height - ldHeight - ldgap;

                int menux = x;
                int menuy = y + iconHeight + gap;
                int menuWidth = width;
                int menuHeight = height - (iconHeight + gap) - (ldHeight + ldgap * 2);
                scroll.setBounds(menux, menuy, menuWidth, menuHeight);

                lightDarkMode.setBounds(ldx, ldy, ldWidth, ldHeight);

                // sin barra de colores ni botón adicional
            }
        }
    }
}

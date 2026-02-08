package com.comercialvalerio.presentation.ui.categorias;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.beans.PropertyChangeListener;
import javax.swing.UIManager;

import com.comercialvalerio.presentation.controller.categorias.CategoriaController;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.application.dto.EstadoNombre;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.DocumentListeners;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.SearchField;
import com.formdev.flatlaf.FlatClientProperties;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import com.comercialvalerio.presentation.ui.common.BottomButtonsPanel;

import net.miginfocom.swing.MigLayout;
import java.util.logging.Logger;

/** Panel que lista las categorías. */
public class FormCategorias extends BaseForm implements Refreshable {

    private final JButton btnNueva  = new JButton("Nueva Categoría");
    private final JButton btnRefresh;
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnActivar = new JButton("Activar");
    private final JButton btnDesactivar = new JButton("Desactivar");
    private final JTable tabla = new NonEditableTable();
    private final JScrollPane sp = new JScrollPane(tabla);
    private final JLabel lblEmpty = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JLabel lblTitulo = new JLabel("Gestión de Categorías");
    private final PropertyChangeListener lafListener = e -> {
        if ("lookAndFeel".equals(e.getPropertyName())) {
            updateTitleStyle();
        }
    };
    private final SearchField searchField = new SearchField("Buscar categoría");
    private final CategoriaController controller;
    private static final Logger LOG = Logger.getLogger(FormCategorias.class.getName());

    public java.util.List<String> getDependencias() { return controller.getDependencias(); }

    public FormCategorias() {
        controller = new CategoriaController(this);
        btnRefresh = UIUtils.createRefreshButton(controller::refresh);
        buildUI();
        KeyUtils.setTooltipAndMnemonic(btnNueva, KeyEvent.VK_N, "Nueva Categoría");
        KeyUtils.setTooltipAndMnemonic(btnEditar, KeyEvent.VK_E, "Editar");
        KeyUtils.setTooltipAndMnemonic(btnEliminar, KeyEvent.VK_L, "Eliminar");
        KeyUtils.setTooltipAndMnemonic(btnActivar, KeyEvent.VK_A, "Activar");
        KeyUtils.setTooltipAndMnemonic(btnDesactivar, KeyEvent.VK_D, "Desactivar");
        boolean admin = controller.isAdmin();
        btnEliminar.setVisible(admin);
        btnActivar.setVisible(admin);
        btnDesactivar.setVisible(admin);
        btnNueva.addActionListener(e -> controller.nueva());
        btnEditar.addActionListener(e -> controller.editarSeleccionado());
        btnEliminar.addActionListener(e -> controller.eliminarSeleccionado());
        btnActivar.addActionListener(e -> controller.activarSeleccionado());
        btnDesactivar.addActionListener(e -> controller.desactivarSeleccionado());
        DocumentListeners.attachDebounced(searchField.getTextField(), controller::buscar);
        searchField.addActionListener(e -> controller.buscar());
        searchField.addClearActionListener(e -> {
            searchField.getTextField().setText("");
            controller.buscar();
        });
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    controller.editarSeleccionado();
                }
            }
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            controller.cargarDependencias();
            updateButtons();
        });
        controller.refresh();
        updateButtons();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        UIManager.addPropertyChangeListener(lafListener);
    }

    @Override
    public void removeNotify() {
        UIManager.removePropertyChangeListener(lafListener);
        super.removeNotify();
    }

    private void buildUI() {
        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow][grow][pref!][pref!]",
                "[][pref!][grow][pref!]"));
        setOpaque(false);
        putClientProperty(FlatClientProperties.STYLE, "background:@background");

        updateTitleStyle();
        add(lblTitulo, "cell 0 0, growx");

        add(btnRefresh, "cell 3 0, right, wrap");

        add(searchField, "cell 0 1, growx, h 40!");

        ButtonStyles.styleHeader(btnNueva, UIStyle.RGB_ACTION_PURPLE);
        add(btnNueva, "cell 3 1, w 190!, h 40!, wrap");

        tabla.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        add(sp, "cell 0 2 4 1, grow");

        ButtonStyles.styleBottom(btnEditar, 0xF47B20,
                "com/comercialvalerio/presentation/ui/icon/svg/edit.svg");
        ButtonStyles.styleBottom(btnActivar, UIStyle.RGB_ACTION_GREEN,
                "com/comercialvalerio/presentation/ui/icon/svg/lock_open.svg");
        ButtonStyles.styleBottom(btnDesactivar, UIStyle.RGB_ACTION_RED,
                "com/comercialvalerio/presentation/ui/icon/svg/lock.svg");
        ButtonStyles.styleBottom(btnEliminar, UIStyle.RGB_ACTION_RED,
                "com/comercialvalerio/presentation/ui/icon/svg/close_circle.svg");
        JPanel south = new BottomButtonsPanel(
                btnEditar, btnActivar, btnDesactivar, btnEliminar);
        add(south, "cell 0 3 4 1, alignx center");

        setBorder(UIStyle.FORM_BORDER_VIOLET);
    }

    public JButton getBtnNueva() { return btnNueva; }

    public JTable getTabla() { return tabla; }

    public JScrollPane getScroll() { return sp; }

    public JLabel getLblEmpty() { return lblEmpty; }

    public JTextField getTxtBuscar() { return searchField.getTextField(); }

    public JButton getBtnEditar() { return btnEditar; }

    public JButton getBtnEliminar() { return btnEliminar; }
    public JButton getBtnActivar() { return btnActivar; }
    public JButton getBtnDesactivar() { return btnDesactivar; }

    @Override
    protected void registerShortcuts() {
        KeyUtils.registerRefreshAction(this, controller::refresh);
        KeyUtils.registerFocusAction(this, searchField.getTextField());
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK),
                controller::nueva);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_DOWN_MASK),
                controller::editarSeleccionado);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK),
                controller::eliminarSeleccionado);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK),
                controller::activarSeleccionado);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK),
                controller::desactivarSeleccionado);
    }

    /** Habilita o deshabilita el botón Editar según la selección de la tabla. */
    public final void updateButtons() {
        int row = tabla.getSelectedRow();
        boolean sel = row >= 0;
        btnEditar.setEnabled(sel);
        btnEliminar.setEnabled(sel && controller.getDependencias().isEmpty());
        btnActivar.setEnabled(false);
        btnDesactivar.setEnabled(false);
        if (sel) {
            Object est = tabla.getModel().getValueAt(row, 2);
            if (est != null && est.toString().equalsIgnoreCase(EstadoNombre.ACTIVO.getNombre())) {
                btnDesactivar.setEnabled(true);
            } else if (est != null) {
                btnActivar.setEnabled(true);
            }
        }
    }

    @Override
    public void refresh() {
        controller.refresh();
    }

    private void updateTitleStyle() {
        lblTitulo.putClientProperty(FlatClientProperties.STYLE,
                "font:$h1.font; foreground:" + UIStyle.getHexDarkText());
    }
}

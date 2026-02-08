package com.comercialvalerio.presentation.ui.empleados;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.formdev.flatlaf.FlatClientProperties;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import javax.swing.UIManager;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import com.comercialvalerio.presentation.ui.util.TableModelUtils;
import com.comercialvalerio.presentation.ui.util.DocumentListeners;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.SearchField;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.application.dto.EstadoNombre;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import com.comercialvalerio.presentation.controller.empleados.EmpleadoController;
import com.comercialvalerio.presentation.ui.common.BottomButtonsPanel;
import java.util.logging.Logger;

/** Panel que lista los empleados. */
public class FormEmpleados extends BaseForm implements Refreshable {

    private static final Logger LOG = Logger.getLogger(FormEmpleados.class.getName());

    private final JButton btnNuevo = new JButton("Nuevo Empleado");
    private final JButton btnRefresh;
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnActivar = new JButton("Activar");
    private final JButton btnDesactivar = new JButton("Desactivar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JLabel lblTitulo = new JLabel("Gestión de Empleados");
    private final PropertyChangeListener lafListener = e -> {
        if ("lookAndFeel".equals(e.getPropertyName())) {
            updateTitleStyle();
        }
    };
    private final JTable tabla = new NonEditableTable();
    private final JScrollPane sp = new JScrollPane(tabla);
    private final JLabel lblEmpty = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private DefaultTableModel model;
    private final SearchField searchField = new SearchField("Ingrese nombre o teléfono");
    private final JComboBox<String> cboEstado =
            new JComboBox<>(new String[]{"Todos",
                    EstadoNombre.ACTIVO.getNombre(),
                    EstadoNombre.INACTIVO.getNombre()});
    private final JLabel lblEstado = new JLabel("Estado:");
    private final EmpleadoController controller = new EmpleadoController(this);

    public FormEmpleados() {
        btnRefresh = UIUtils.createRefreshButton(controller::refresh);
        buildUI();
        KeyUtils.setTooltipAndMnemonic(btnNuevo, KeyEvent.VK_N, "Nuevo Empleado");
        KeyUtils.setTooltipAndMnemonic(btnEditar, KeyEvent.VK_E, "Editar");
        KeyUtils.setTooltipAndMnemonic(btnActivar, KeyEvent.VK_A, "Activar");
        KeyUtils.setTooltipAndMnemonic(btnDesactivar, KeyEvent.VK_D, "Desactivar");
        KeyUtils.setTooltipAndMnemonic(btnEliminar, KeyEvent.VK_L, "Eliminar");

        boolean admin = controller.isAdmin();
        btnEliminar.setVisible(admin);
        btnActivar.setVisible(admin);
        btnDesactivar.setVisible(admin);
        String[] cols = {"ID","Nombre","DNI","Teléfono","Usuario","Rol","Estado"};
        model = TableModelUtils.createModel(tabla, cols, new int[]{2,3}, 0);
        tabla.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        sp.setBorder(null);

        DocumentListeners.attachDebounced(searchField.getTextField(), controller::refresh);
        searchField.addActionListener(e -> controller.refresh());
        cboEstado.addActionListener(e -> controller.refresh());
        searchField.addClearActionListener(e -> {
            searchField.getTextField().setText("");
            controller.refresh();
        });

        btnNuevo.addActionListener(e -> controller.nuevo());
        btnEditar.addActionListener(e -> controller.editarSeleccionado());
        btnActivar.addActionListener(e -> controller.activarSeleccionado());
        btnDesactivar.addActionListener(e -> controller.bajaSeleccionado());
        btnEliminar.addActionListener(e -> controller.eliminarSeleccionado());
        tabla.getSelectionModel().addListSelectionListener(e -> updateButtons());
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    controller.editarSeleccionado();
                }
            }
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
        add(lblTitulo, "cell 0 0 3 1, growx");

        add(btnRefresh, "cell 3 0, right, wrap");

        add(searchField, "cell 0 1, growx, h 40!");

        cboEstado.putClientProperty(FlatClientProperties.STYLE,
                "arc:" + UIStyle.ARC_DEFAULT);
        cboEstado.setPrototypeDisplayValue(EstadoNombre.INACTIVO.getNombre());
        lblEstado.setLabelFor(cboEstado);
        add(lblEstado, "cell 1 1, split 2, aligny center");
        add(cboEstado, "h " + UIStyle.COMBO_HEIGHT + "!, growx 0");

        ButtonStyles.styleHeader(btnNuevo, UIStyle.RGB_ACTION_PURPLE);
        add(btnNuevo, "cell 3 1, w 190!, h 40!, wrap");

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

    public JButton getBtnNuevo() {
        return btnNuevo;
    }

    public JTable getTabla() {
        return tabla;
    }

    public JScrollPane getScrollPane() { return sp; }

    public JLabel getLblEmpty() { return lblEmpty; }

    public JButton getBtnRefresh() {
        return btnRefresh;
    }

    public JTextField getTxtBuscar() { return searchField.getTextField(); }

    public JComboBox<String> getCboEstado() { return cboEstado; }

    public JButton getBtnEditar() { return btnEditar; }
    public JButton getBtnActivar() { return btnActivar; }
    public JButton getBtnDesactivar() { return btnDesactivar; }
    public JButton getBtnEliminar() { return btnEliminar; }

    public java.util.List<String> getDependencias() { return controller.getDependencias(); }

    /** Habilita o deshabilita las acciones según la selección de la tabla. */
    public void updateButtons() {
        int row = tabla.getSelectedRow();
        boolean sel = row >= 0;
        btnEditar.setEnabled(sel);
        btnActivar.setEnabled(false);
        btnDesactivar.setEnabled(false);
        btnEliminar.setEnabled(sel && controller.getDependencias().isEmpty());
        if (sel) {
            int modelRow = tabla.convertRowIndexToModel(row);
            Object est = tabla.getModel().getValueAt(modelRow, 6);
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

    @Override
    protected void registerShortcuts() {
        KeyUtils.registerRefreshAction(this, controller::refresh);
        KeyUtils.registerFocusAction(this, searchField.getTextField());
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK),
                controller::nuevo);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_DOWN_MASK),
                controller::editarSeleccionado);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK),
                controller::activarSeleccionado);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK),
                controller::bajaSeleccionado);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK),
                controller::eliminarSeleccionado);
    }

    private void updateTitleStyle() {
        lblTitulo.putClientProperty(FlatClientProperties.STYLE,
                "font:$h1.font; foreground:" + UIStyle.getHexDarkText());
    }
}

package com.comercialvalerio.presentation.ui.productos;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.formdev.flatlaf.FlatClientProperties;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import java.awt.*;
import java.util.Locale;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import javax.swing.border.*;
import com.comercialvalerio.presentation.controller.productos.ProductoController;
import com.comercialvalerio.presentation.ui.util.DocumentListeners;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.SearchField;
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import com.comercialvalerio.presentation.ui.common.BottomButtonsPanel;
import com.comercialvalerio.application.dto.EstadoNombre;
import net.miginfocom.swing.MigLayout;

public class FormGestionProductos extends BaseForm implements Refreshable {

    /* widgets */
    private final SearchField       searchField      = new SearchField("Buscar producto");
    private final JComboBox<String> cboCategoria     = new JComboBox<>();
    private final JComboBox<String> cboTipo          = new JComboBox<>();
    private final JComboBox<String> cboEstado        = new JComboBox<>(new String[]{
            "Todos",
            EstadoNombre.ACTIVO.getNombre(),
            EstadoNombre.INACTIVO.getNombre(),
            EstadoNombre.INACTIVO_POR_UMBRAL.getNombre()});
    private final JLabel            lblCategoria     = new JLabel("Categoría:");
    private final JLabel            lblTipo          = new JLabel("Tipo de producto:");
    private final JLabel            lblEstado        = new JLabel("Estado:");
    private final JTable            tblProductos     = new NonEditableTable();
    private final JScrollPane       spProductos      = new JScrollPane(tblProductos);
    private final JLabel            lblEmpty         = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);

    private final JButton btnRefresh;
    private final JButton btnNuevoProducto  = new JButton("Nuevo Producto");
    private final JButton btnNuevaCategoria = new JButton("Nueva Categoría");
    private final JButton btnVerDetalle     = new JButton("Ver detalle");
    private final JButton btnEditar         = new JButton("Editar");
    private final JButton btnActivar        = new JButton("Activar");
    private final JButton btnDesactivar     = new JButton("Desactivar");
    private final JButton btnEliminar       = new JButton("Eliminar");

    private final ProductoController controller;

    public FormGestionProductos() {
        controller = new ProductoController(this);
        btnRefresh = UIUtils.createRefreshButton(controller::refresh);
        buildUI();
        controller.cargarCategorias();
        controller.cargarTipos();
        controller.refresh();

        DocumentListeners.attachDebounced(searchField.getTextField(), controller::buscar);
        searchField.addActionListener(e -> controller.buscar());

        searchField.addClearActionListener(e -> {
            searchField.getTextField().setText("");
            controller.refresh();
        });

        cboCategoria.addActionListener(e -> controller.buscar());
        cboTipo.addActionListener(e -> controller.buscar());
        cboEstado.addActionListener(e -> controller.buscar());
        btnNuevoProducto.addActionListener(e -> controller.nuevo());
        btnNuevaCategoria.addActionListener(e -> controller.nuevaCategoria());
        btnVerDetalle.addActionListener(e -> controller.verDetalle());
        btnEditar.addActionListener(e -> controller.editarSeleccionado());
        btnActivar.addActionListener(e -> controller.activarSeleccionado());
        btnDesactivar.addActionListener(e -> controller.desactivarSeleccionado());
        btnEliminar.addActionListener(e -> controller.eliminarSeleccionado());
        boolean admin = controller.isAdmin();
        btnEliminar.setVisible(admin);
        btnActivar.setVisible(admin);
        // Empleados también pueden desactivar productos
        btnDesactivar.setVisible(true);
        tblProductos.getSelectionModel().addListSelectionListener(e -> {
            cargarDependencias();
            updateButtons();
        });
        tblProductos.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblProductos.getSelectedRow();
                    if (row >= 0) {
                        int modelRow = tblProductos.convertRowIndexToModel(row);
                        String tipo = (String) tblProductos.getModel().getValueAt(modelRow, 3);
                        if (!"Unidad fija".equalsIgnoreCase(tipo)) {
                            controller.verDetalle();
                        }
                    }
                }
            }
        });
        updateButtons();
    }

    private void buildUI() {
        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]", "[]10[grow]10[]push"));
        setOpaque(false);
        putClientProperty(FlatClientProperties.STYLE, "background:@background");

        /* ---------- Encabezado ------------------------------------------------ */
        HeaderPanel header = new HeaderPanel("Gestión de Productos", btnRefresh);
        header.setBorder(new EmptyBorder(0,0,5,0));

        add(header, "cell 0 0, growx, wrap");

        /* ---------- Cuerpo ---------------------------------------------------- */
        JPanel body = new JPanel(new MigLayout(
            "insets 0, gap " + UIStyle.PANEL_GAP + ", fill",
            "[grow,fill]",                 // una sola columna
            "[]10[grow]10[]push"));        // filtros, tabla, botones
        body.setOpaque(false);
        add(body, "cell 0 1, grow");

        /* ------ Filtros + acciones (fila 0) ---------------------------------- */
        JPanel rowTop = new JPanel(new MigLayout(
            "insets 0, gap " + UIStyle.PANEL_GAP + ", fill",
            // buscador | etiquetas y combos | botones
            "[grow,fill][pref!][160!][pref!][160!][pref!][160!]20[]10[]",
            "[]"));
        rowTop.setOpaque(false);

        // buscador con ancho ligeramente mayor
        rowTop.add(searchField, "cell 0 0, w 320!, h 40!");

        // combo categorías al lado
        cboCategoria.setPrototypeDisplayValue("XXXXXXXXXXXX");
        cboCategoria.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Categoría");
        lblCategoria.setLabelFor(cboCategoria);
        rowTop.add(lblCategoria, "cell 1 0, aligny center");
        rowTop.add(cboCategoria, "cell 2 0, h " + UIStyle.COMBO_HEIGHT + "!");

        cboTipo.setPrototypeDisplayValue("XXXXXXXXXXXX");
        cboTipo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Tipo de producto");
        lblTipo.setLabelFor(cboTipo);
        rowTop.add(lblTipo, "cell 3 0, aligny center");
        rowTop.add(cboTipo, "cell 4 0, h " + UIStyle.COMBO_HEIGHT + "!");

        cboEstado.putClientProperty(FlatClientProperties.STYLE,
                "arc:" + UIStyle.ARC_DEFAULT);
        cboEstado.setPrototypeDisplayValue(
                EstadoNombre.INACTIVO_POR_UMBRAL.getNombre());
        lblEstado.setLabelFor(cboEstado);
        rowTop.add(lblEstado, "cell 5 0, aligny center");
        rowTop.add(cboEstado, "cell 6 0, h " + UIStyle.COMBO_HEIGHT + "!");

        // botones Nuevo Producto / Nueva Categoría pegados a la izquierda
        ButtonStyles.styleHeader(btnNuevoProducto,  0x2ECC71);
        ButtonStyles.styleHeader(btnNuevaCategoria, 0x7259C4);
        KeyUtils.setTooltipAndMnemonic(btnNuevoProducto, KeyEvent.VK_N, "Nuevo Producto");
        KeyUtils.setTooltipAndMnemonic(btnNuevaCategoria, KeyEvent.VK_C, "Nueva Categoría");
        rowTop.add(btnNuevoProducto,  "cell 7 0");
        rowTop.add(btnNuevaCategoria, "cell 8 0");

        body.add(rowTop, "cell 0 0, growx, wrap");

        /* ------ Tabla productos (fila 1) -------------------------------------- */
        tblProductos.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        body.add(spProductos, "cell 0 1, grow");

        /* ------ Botones inferiores (fila 2) ----------------------------------- */
        ButtonStyles.styleBottom(btnVerDetalle,  UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/eye.svg");
        ButtonStyles.styleBottom(btnEditar,      0xF39C12, "com/comercialvalerio/presentation/ui/icon/svg/edit.svg");
        ButtonStyles.styleBottom(
                btnActivar, 0x27AE60,
                "com/comercialvalerio/presentation/ui/icon/svg/lock_open.svg");
        KeyUtils.setTooltipAndMnemonic(btnVerDetalle, KeyEvent.VK_V, "Ver detalle");
        KeyUtils.setTooltipAndMnemonic(btnEditar, KeyEvent.VK_E, "Editar");
        KeyUtils.setTooltipAndMnemonic(btnActivar, KeyEvent.VK_A, "Activar");
        KeyUtils.setTooltipAndMnemonic(btnDesactivar, KeyEvent.VK_D, "Desactivar");
        ButtonStyles.styleBottom(btnDesactivar,  0xC0392B, "com/comercialvalerio/presentation/ui/icon/svg/lock.svg");
        ButtonStyles.styleBottom(btnEliminar,    UIStyle.RGB_ACTION_RED, "com/comercialvalerio/presentation/ui/icon/svg/close_circle.svg");
        KeyUtils.setTooltipAndMnemonic(btnEliminar, KeyEvent.VK_L, "Eliminar");

        JPanel south = new BottomButtonsPanel(
                btnVerDetalle, btnEditar, btnActivar, btnDesactivar, btnEliminar);
        body.add(south, "cell 0 2, alignx center");

        /* ---------- Borde exterior -------------------------------------------- */
        setBorder(UIStyle.FORM_BORDER_VIOLET);
    }

    /* getters (accesores) */
    public JTextField        getTxtBuscar()        { return searchField.getTextField(); }
    public JComboBox<String> getCboCategoria()     { return cboCategoria;     }
    public JComboBox<String> getCboTipo()          { return cboTipo;          }
    public JComboBox<String> getCboEstado()        { return cboEstado;        }
    public JTable            getTblProductos()     { return tblProductos;     }
    public JScrollPane       getSpProductos()      { return spProductos;      }
    public JLabel            getLblEmpty()         { return lblEmpty;         }
    public JButton           getBtnRefresh()       { return btnRefresh;       }
    public JButton           getBtnNuevoProducto() { return btnNuevoProducto; }
    public JButton           getBtnNuevaCategoria(){ return btnNuevaCategoria;}
    public JButton           getBtnVerDetalle()   { return btnVerDetalle;   }
    public JButton           getBtnEditar()        { return btnEditar;        }
    public JButton           getBtnActivar()       { return btnActivar;       }
    public JButton           getBtnDesactivar()    { return btnDesactivar;    }
    public JButton           getBtnEliminar()      { return btnEliminar;      }

    public java.util.List<String> getDependencias() {
        return controller.getDependencias();
    }

    @Override
    protected void registerShortcuts() {
        KeyUtils.registerRefreshAction(this, controller::refresh);
        KeyUtils.registerFocusAction(this, searchField.getTextField());
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK),
                controller::nuevo);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK),
                controller::nuevaCategoria);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.ALT_DOWN_MASK),
                controller::verDetalle);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_DOWN_MASK),
                controller::editarSeleccionado);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK),
                controller::activarSeleccionado);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK),
                controller::desactivarSeleccionado);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK),
                controller::eliminarSeleccionado);
    }

    private void cargarDependencias() {
        controller.cargarDependencias();
    }

    /** Habilita o deshabilita acciones según la fila seleccionada. */
    public final void updateButtons() {
        int row = tblProductos.getSelectedRow();
        boolean sel = row >= 0;
        boolean detalle = false;
        if (sel) {
            int modelRow = tblProductos.convertRowIndexToModel(row);
            Object tipo = tblProductos.getModel().getValueAt(modelRow, 3);
            if (tipo != null) {
                String t = tipo.toString().toLowerCase(Locale.ROOT);
                detalle = t.equals("vestimenta") || t.equals("fraccionable");
            }
        }
        btnVerDetalle.setEnabled(detalle);
        btnEditar.setEnabled(sel);
        btnActivar.setEnabled(false);
        btnDesactivar.setEnabled(false);
        boolean admin = controller.isAdmin();
        btnEliminar.setVisible(admin);
        btnActivar.setVisible(admin);
        // empleados también pueden desactivar productos
        btnDesactivar.setVisible(true);
        btnEliminar.setEnabled(sel && admin && controller.getDependencias().isEmpty());
        if (sel) {
            int modelRow = tblProductos.convertRowIndexToModel(row);
            Object est = tblProductos.getModel().getValueAt(modelRow, 6);
            if (est != null && est.toString().equalsIgnoreCase("Activo")) {
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
}

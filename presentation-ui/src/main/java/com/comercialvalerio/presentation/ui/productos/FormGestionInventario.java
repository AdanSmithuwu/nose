package com.comercialvalerio.presentation.ui.productos;

import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;

import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.presentation.controller.productos.GestionInventarioController;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.DigitFilter;
import com.comercialvalerio.presentation.ui.util.DocumentListeners;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.SearchField;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

public final class FormGestionInventario extends BaseForm implements Refreshable {

    /* widgets públicos */
    private final JSpinner          spnCantidad    =
            new JSpinner(new SpinnerNumberModel(1, 1, DbConstraints.MAX_CANTIDAD, 1));
    private final SearchField       searchField   = new SearchField("Buscar producto");
    private final JComboBox<String> cboCategoria   = new JComboBox<>();
    private final JTable            tblInventario  = new NonEditableTable();
    private final JComboBox<String> cboTalla       = new JComboBox<>();
    private final JComboBox<String> cboPresentacion= new JComboBox<>();
    private final JLabel            lblCategoria   = new JLabel("Categoría:");
    private final JLabel            lblTalla       = new JLabel("Talla:");
    private final JLabel            lblPresentacion= new JLabel("Presentación:");
    private final JSpinner          spnVeces       = new JSpinner(new SpinnerNumberModel(1,1,9999,1));
    private final JLabel            lblCantidad    = new JLabel("Cantidad:");
    private final JScrollPane       spInventario   = new JScrollPane(tblInventario);
    private final JLabel            lblEmpty       = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);

    private final JButton btnIngresar    = new JButton("Ingresar");
    private final JButton btnAjustar     = new JButton("Ajustar");
    private final JButton btnRefresh;

    private final GestionInventarioController controller;
    private static final Logger LOG = Logger.getLogger(FormGestionInventario.class.getName());

    public FormGestionInventario() {
        controller = new GestionInventarioController(this);
        btnRefresh = UIUtils.createRefreshButton(controller::cargarProductos);
        buildUI();
        var txt = ((JSpinner.DefaultEditor) spnCantidad.getEditor()).getTextField();
        ((AbstractDocument) txt.getDocument())
                .setDocumentFilter(new DigitFilter(DbConstraints.CANTIDAD_INTEGER, txt));
        controller.cargarCategorias();
        controller.cargarProductos();
        cboCategoria.addActionListener(e -> controller.cargarProductos());
        DocumentListeners.attachDebounced(searchField.getTextField(),
                () -> controller.buscarProductos(searchField.getText().trim()));
        searchField.addActionListener(e ->
                controller.buscarProductos(searchField.getText().trim()));
        searchField.addClearActionListener(e -> {
            searchField.getTextField().setText("");
            controller.cargarProductos();
        });
        btnIngresar.addActionListener(e -> controller.ingresarStock());
        btnAjustar.addActionListener(e -> controller.ajustarStock());
        tblInventario.getSelectionModel().addListSelectionListener(e -> {
            updateButtons();
            controller.cargarExtras();
        });
        spnCantidad.addChangeListener(e -> updateButtons());
        updateButtons();
    }

    @Override
    protected void registerShortcuts() {
        KeyUtils.registerRefreshAction(this, controller::cargarProductos);
        KeyUtils.registerFocusAction(this, searchField.getTextField());
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.ALT_DOWN_MASK),
                controller::ingresarStock);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK),
                controller::ajustarStock);
    }

    private void buildUI() {
        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]", "[]10[grow]"));
        setOpaque(false);
        putClientProperty(FlatClientProperties.STYLE, "background:@background");

        // Encabezado
        HeaderPanel header = new HeaderPanel("Gestión de Inventario", btnRefresh);
        header.setBorder(new EmptyBorder(0,0,5,0));
        add(header, "cell 0 0, growx, wrap");

        /* ---------------- cuerpo ----------------------------------------- */
        JPanel body = new JPanel(new MigLayout(
                // col0 = controles  col1 = zona tabla
                "insets 0, gap 20, fill",
                "[grow][grow,fill]",
                "[]10[grow]push"));
        body.setOpaque(false);
        add(body, "cell 0 1, grow");

        /* ---------- fila 0 (filtros) ----------------------------------- */

        // placeholder y estilo configurados en el constructor de SearchField

        cboCategoria.setPrototypeDisplayValue("XXXXXXXXXXXX");
        cboCategoria.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Categoría");
        cboCategoria.putClientProperty(FlatClientProperties.STYLE, "arc:" + UIStyle.ARC_DIALOG);
        lblCategoria.setLabelFor(cboCategoria);

        body.add(searchField, "cell 1 0, split 3, growx, h 40!");
        body.add(lblCategoria, "gapleft 5, aligny center");
        body.add(cboCategoria, "gapleft 5, aligny center, h " + UIStyle.COMBO_HEIGHT + "!, w 160!, wrap");

        /* ---------- panel botones izquierda (fila 1) -------------------- */
        JPanel left = new JPanel(new MigLayout(
                "fillx, wrap 2, insets 0, gap " + UIStyle.PANEL_GAP,
                "[right][grow]"));
        left.setOpaque(false);
        cboTalla.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Talla");
        cboTalla.putClientProperty(FlatClientProperties.STYLE, "arc:" + UIStyle.ARC_DIALOG);
        cboPresentacion.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,"Presentación");
        cboPresentacion.putClientProperty(FlatClientProperties.STYLE,"arc:" + UIStyle.ARC_DIALOG);
        cboTalla.setVisible(false);
        lblTalla.setVisible(false);
        cboPresentacion.setVisible(false);
        lblPresentacion.setVisible(false);
        spnVeces.setVisible(false);
        spnVeces.setPreferredSize(new Dimension(80,30));
        lblTalla.setLabelFor(cboTalla);
        lblPresentacion.setLabelFor(cboPresentacion);
        left.add(lblCantidad);
        spnCantidad.setPreferredSize(new Dimension(120,30));
        left.add(spnCantidad, "w 120!, h 30!, wrap");
        left.add(lblTalla);
        left.add(cboTalla, "w 160!, h " + UIStyle.COMBO_HEIGHT + "!, wrap");
        left.add(lblPresentacion);
        left.add(cboPresentacion, "w 160!, h " + UIStyle.COMBO_HEIGHT + "!");
        left.add(spnVeces, "w 80!, wrap");
        ButtonStyles.styleBottom(btnIngresar, 0x2ECC71, "com/comercialvalerio/presentation/ui/icon/svg/plus.svg");
        ButtonStyles.styleBottom(btnAjustar , 0xE74C3C, "com/comercialvalerio/presentation/ui/icon/svg/minus.svg");
        KeyUtils.setTooltipAndMnemonic(btnIngresar, KeyEvent.VK_I, "Ingresar");
        KeyUtils.setTooltipAndMnemonic(btnAjustar, KeyEvent.VK_A, "Ajustar");
        left.add(btnIngresar, "span 2, growx, wrap");
        left.add(btnAjustar , "span 2, growx, wrap");
        body.add(left, "cell 0 1, grow, alignx center");

        /* ---------- tabla (fila 1, col 1) ------------------------------- */
        tblInventario.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        body.add(spInventario, "cell 1 1, grow");

        /* -------- borde exterior ---------------------------------------- */
        setBorder(UIStyle.FORM_BORDER_VIOLET);
    }

    // Getters para controlador
    public JSpinner          getSpnCantidad()   { return spnCantidad;   }
    public JTextField        getTxtBuscar()     { return searchField.getTextField(); }
    public JComboBox<String> getCboCategoria()  { return cboCategoria;  }
    public JTable            getTblInventario() { return tblInventario; }
    public JScrollPane       getSpInventario()  { return spInventario;  }
    public JComboBox<String> getCboTalla()      { return cboTalla;      }
    public JComboBox<String> getCboPresentacion() { return cboPresentacion; }
    public JSpinner          getSpnVeces()      { return spnVeces;      }
    public JLabel            getLblCantidad()   { return lblCantidad;   }
    public JLabel            getLblTalla()      { return lblTalla;      }
    public JLabel            getLblPresentacion() { return lblPresentacion; }
    public JLabel            getLblEmpty()      { return lblEmpty;      }
    public JButton           getBtnRefresh()    { return btnRefresh;    }
    public JButton           getBtnIngresar()   { return btnIngresar;   }
    public JButton           getBtnAjustar()    { return btnAjustar;    }

    /** Habilita o deshabilita acciones según la selección y la cantidad. */
    public final void updateButtons() {
        int row = tblInventario.getSelectedRow();
        boolean sel = row >= 0;
        int qty = ((Number) spnCantidad.getValue()).intValue();
        boolean enable = sel && qty > 0;
        btnIngresar.setEnabled(enable);
        boolean admin = controller.isAdmin();
        btnAjustar.setVisible(admin);
        boolean stock = true;
        if (sel) {
            int modelRow = tblInventario.convertRowIndexToModel(row);
            Object val = tblInventario.getModel().getValueAt(modelRow, 3);
            if (val instanceof Number n) {
                stock = n.doubleValue() > 0;
            }
        }
        btnAjustar.setEnabled(enable && admin && stock);
    }

    @Override
    public void refresh() {
        controller.cargarProductos();
    }
}

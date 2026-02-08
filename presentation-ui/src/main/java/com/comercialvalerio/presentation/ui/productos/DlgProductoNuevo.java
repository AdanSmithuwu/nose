package com.comercialvalerio.presentation.ui.productos;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.comercialvalerio.application.dto.CategoriaDto;
import com.comercialvalerio.application.dto.TipoProductoDto;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.NumericFilter;
import com.comercialvalerio.presentation.ui.util.NumericVerifier;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.RequiredVerifier;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import com.comercialvalerio.presentation.controller.productos.ProductoController;
import com.comercialvalerio.application.dto.TipoPedido;
import com.comercialvalerio.presentation.ui.util.LengthFilter;
import com.comercialvalerio.common.DbConstraints;
import javax.swing.text.AbstractDocument;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.comercialvalerio.presentation.ui.base.BaseDialog;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import javax.swing.border.EmptyBorder;

/** Diálogo para registrar un nuevo producto. */
public class DlgProductoNuevo extends BaseDialog {

    /** Indica si el diálogo está en modo edición. */
    protected final boolean editing;

    private final JTextField txtNombre = new JTextField();
    private final JTextArea  txtDescripcion = new JTextArea(3, 20);
    private final JComboBox<CategoriaDto> cboCategoria = new JComboBox<>();
    private final JComboBox<TipoProductoDto> cboTipo = new JComboBox<>();
    private final JTextField txtUnidad = new JTextField();
    private final JTextField txtPrecioUnitario = new JTextField();
    private final JCheckBox chkMayorista = new JCheckBox("Mayorista");
    private final JCheckBox chkParaPedido = new JCheckBox("Para pedido");
    private final JComboBox<TipoPedido> cboTipoPedido = new JComboBox<>(TipoPedido.values());
    private final JSpinner spnMinMayorista = new JSpinner(new SpinnerNumberModel(1,1,Integer.MAX_VALUE,1));
    private final JTextField txtPrecioMayorista = new JTextField();
    private final JLabel lblTipoPedido = new JLabel("Tipo Pedido:");
    private final JLabel lblMinMayorista = new JLabel("Min. Mayorista:");
    private final JLabel lblPrecioMayorista = new JLabel("Precio Mayorista:");
    private final JLabel lblStockInicial = new JLabel("Stock Inicial:");
    private final JTextField txtStockActual = new JTextField();
    private final JTextField txtUmbral = new JTextField();
    private final JButton btnGuardar;
    private final JButton btnRefresh = UIUtils.createRefreshButton(this::reloadCombos);

    private final DefaultTableModel modelTallas =
            new DefaultTableModel(new String[]{"ID","Talla","Stock","Estado"},0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    if (editing && column == 2) {
                        return false; // Stock bloqueado en edición
                    }
                    return column != 0 && column != 3;
                }
            };
    private final JTable tblTallas = new JTable(modelTallas);
    private final DefaultTableModel modelPres =
            new DefaultTableModel(new String[]{"ID","Cantidad","Precio","Estado"},0);
    private final JTable tblPresentaciones = new JTable(modelPres);
    private final JPanel pnlExtras = new JPanel(new CardLayout());
    /** Cabecera reutilizable que muestra el título del diálogo. */
    protected HeaderPanel header;
    private ProductoController controller;
    private java.util.Set<Integer> tallasBloqueadas = java.util.Set.of();

    private Dimension minSizeBase;
    private Dimension minSizeExtras;

    public DlgProductoNuevo(JFrame owner) {
        this(owner, false);
    }

    protected DlgProductoNuevo(JFrame owner, boolean editing) {
        super(owner, editing ? "Editar Producto" : "Nuevo Producto", true,
                new JButton(editing ? "Guardar" : "Registrar"));
        this.editing = editing;
        this.btnGuardar = getDefaultButton();
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptCancel();
            }
        });
        buildUI();
        SwingUtilities.invokeLater(() -> txtNombre.requestFocusInWindow());
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptCancel();
            }
        });
        minSizeBase = getPreferredSize();
        minSizeExtras = computeMaxSize();
        setMinimumSize(minSizeBase);
        pack();
        DialogUtils.ensureMinWidth(this);
        setLocationRelativeTo(owner);

    }

    private void buildUI() {
        JPanel form = new JPanel(new net.miginfocom.swing.MigLayout(
                "fillx, wrap 2, insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow,fill][grow,fill]"));
        form.putClientProperty(
                FlatClientProperties.STYLE,
                "background:@background" + "; arc:" + UIStyle.ARC_DEFAULT);

        JPanel left = new JPanel(new net.miginfocom.swing.MigLayout(
                "fillx, wrap 2", "[right][grow,fill]"));
        JPanel right = new JPanel(new net.miginfocom.swing.MigLayout(
                "fillx, wrap 2", "[right][grow,fill]"));

        ((AbstractDocument) txtNombre.getDocument())
                .setDocumentFilter(new LengthFilter(DbConstraints.LEN_NOMBRE_PRODUCTO));
        ((AbstractDocument) txtDescripcion.getDocument())
                .setDocumentFilter(new LengthFilter(DbConstraints.LEN_DESCRIPCION));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        ((AbstractDocument) txtUnidad.getDocument())
                .setDocumentFilter(new LengthFilter(DbConstraints.LEN_UNIDAD_MEDIDA));

        RequiredVerifier req = new RequiredVerifier();
        NumericVerifier num = new NumericVerifier();
        txtNombre.setInputVerifier(req);
        cboCategoria.setInputVerifier(req);
        cboTipo.setInputVerifier(req);
        cboCategoria.setPrototypeDisplayValue(new CategoriaDto(0, "XXXXXXXXXXXX", "", ""));
        cboTipo.setPrototypeDisplayValue(new TipoProductoDto(0, "XXXXXXXXXXXX"));
        cboTipoPedido.setPrototypeDisplayValue(TipoPedido.ESPECIAL);
        txtUnidad.setInputVerifier(req);
        txtPrecioUnitario.setInputVerifier(num);
        txtPrecioMayorista.setInputVerifier(num);
        txtUmbral.setInputVerifier(num);
        ((AbstractDocument) txtPrecioUnitario.getDocument())
                .setDocumentFilter(new NumericFilter(8, txtPrecioUnitario));
        ((AbstractDocument) txtPrecioMayorista.getDocument()).setDocumentFilter(new NumericFilter(txtPrecioMayorista));
        ((AbstractDocument) txtStockActual.getDocument()).setDocumentFilter(new NumericFilter(txtStockActual));
        ((AbstractDocument) txtUmbral.getDocument())
                .setDocumentFilter(new NumericFilter(2, txtUmbral));

        left.add(new JLabel("Nombre:"));            left.add(txtNombre, "growx, wrap");
        left.add(new JLabel("Descripción:"));       left.add(new JScrollPane(txtDescripcion), "growx, h 60!, wrap");
        left.add(new JLabel("Categoría:"));         left.add(cboCategoria, "growx, wrap");
        left.add(new JLabel("Tipo:"));              left.add(cboTipo, "growx, wrap");
        left.add(new JLabel("Unidad:"));            left.add(txtUnidad, "growx, wrap");
        left.add(new JLabel("Precio Unitario:"));   left.add(txtPrecioUnitario, "growx, wrap");

        right.add(chkMayorista, "split 2");          right.add(chkParaPedido, "wrap");
        right.add(lblTipoPedido);                    right.add(cboTipoPedido, "growx, wrap");
        right.add(lblMinMayorista);                  right.add(spnMinMayorista, "growx, wrap");
        right.add(lblPrecioMayorista);               right.add(txtPrecioMayorista, "growx, wrap");
        right.add(lblStockInicial);                  right.add(txtStockActual, "growx, wrap");
        right.add(new JLabel("Umbral:"));           right.add(txtUmbral, "growx, wrap");

        form.add(left, "growx");
        form.add(right, "growx, wrap");

        // Panel de extras
        JPanel none = new JPanel();
        JPanel tallas = buildTallasPanel();
        JPanel presentaciones = buildPresentacionesPanel();
        pnlExtras.add(none, "NONE");
        pnlExtras.add(tallas, "VEST");
        pnlExtras.add(presentaciones, "FRACC");
        form.add(pnlExtras, "span 2, growx, wrap");

        ButtonStyles.styleBottom(btnGuardar, UIStyle.RGB_ACTION_GREEN,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        KeyUtils.setTooltipAndMnemonic(btnGuardar, KeyEvent.VK_R, "Registrar");
        form.add(btnGuardar, "span, right");

        JPanel panel = new JPanel(new BorderLayout());
        header = new HeaderPanel("Registro de Producto",
                new EmptyBorder(10,20,5,32), btnRefresh);
        panel.add(header, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        setContentPane(panel);

        showExtras(null);
        updatePedidoFields();
        updateMayoristaFields();
        cboTipo.addActionListener(e -> {
            TipoProductoDto sel = (TipoProductoDto) cboTipo.getSelectedItem();
            showExtras(sel==null?null:sel.nombre());
        });
        chkParaPedido.addItemListener(e -> updatePedidoFields());
        chkMayorista.addItemListener(e -> updateMayoristaFields());
    }

    private JPanel buildTallasPanel() {
        JPanel panel = new JPanel(new BorderLayout(5,5));
        tblTallas.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        panel.add(new JScrollPane(tblTallas), BorderLayout.CENTER);
        if (tblTallas.getColumnCount() > 0) {
            tblTallas.getColumnModel().removeColumn(
                    tblTallas.getColumnModel().getColumn(0));
        }
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0));
        JButton add = new JButton();
        add.setIcon(new FlatSVGIcon("com/comercialvalerio/presentation/ui/icon/svg/plus.svg"));
        KeyStroke addKey = KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.ALT_DOWN_MASK);
        KeyStroke addKey2 = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
                InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        KeyUtils.setTooltipAndMnemonic(add, "Añadir fila", addKey, addKey2);
        JButton del = new JButton();
        del.setIcon(new FlatSVGIcon("com/comercialvalerio/presentation/ui/icon/svg/minus.svg"));
        KeyStroke delKey = KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.ALT_DOWN_MASK);
        KeyStroke delKey2 = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.ALT_DOWN_MASK);
        KeyUtils.setTooltipAndMnemonic(del, "Quitar fila", delKey, delKey2);
        final JButton act = editing ? new JButton() : null;
        final JButton des = editing ? new JButton() : null;
        final KeyStroke actKey = editing ? KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK) : null;
        final KeyStroke desKey = editing ? KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK) : null;
        if (editing) {
            act.setIcon(new FlatSVGIcon("com/comercialvalerio/presentation/ui/icon/svg/lock_open.svg"));
            KeyUtils.setTooltipAndMnemonic(act, "Activar", actKey);
            des.setIcon(new FlatSVGIcon("com/comercialvalerio/presentation/ui/icon/svg/lock.svg"));
            KeyUtils.setTooltipAndMnemonic(des, "Desactivar", desKey);
        }
        del.setEnabled(false);
        if (des != null) des.setEnabled(false);
        if (act != null) act.setEnabled(false);
        add.addActionListener(e -> modelTallas.addRow(new Object[]{null,"","","Activo"}));
        del.addActionListener(e -> {
            int r = tblTallas.getSelectedRow();
            if (r>=0) modelTallas.removeRow(r);
        });
        if (des != null) {
            des.addActionListener(e -> {
                int r = tblTallas.getSelectedRow();
                if (r>=0 && controller != null) controller.desactivarTalla(this);
            });
        }
        if (act != null) {
            act.addActionListener(e -> {
                int r = tblTallas.getSelectedRow();
                if (r>=0 && controller != null) controller.activarTalla(this);
            });
        }
        KeyUtils.registerKeyAction(getRootPane(),
                addKey,
                add::doClick);
        KeyUtils.registerKeyAction(getRootPane(),
                addKey2,
                add::doClick);
        KeyUtils.registerKeyAction(getRootPane(),
                delKey,
                del::doClick);
        KeyUtils.registerKeyAction(getRootPane(),
                delKey2,
                del::doClick);
        if (des != null) {
            KeyUtils.registerKeyAction(getRootPane(), desKey, des::doClick);
        }
        if (act != null) {
            KeyUtils.registerKeyAction(getRootPane(), actKey, act::doClick);
        }
        tblTallas.getSelectionModel().addListSelectionListener(e ->
        {
            int r = tblTallas.getSelectedRow();
            if (r >= 0) {
                Integer id = (Integer) modelTallas.getValueAt(r, 0);
                boolean bloqueada = id != null && tallasBloqueadas.contains(id);
                del.setEnabled(!bloqueada);
                if (editing) {
                    String est = modelTallas.getValueAt(r,3).toString();
                    boolean activo = "Activo".equalsIgnoreCase(est);
                    if (des != null) des.setEnabled(activo);
                    if (act != null) act.setEnabled(!activo);
                }
            } else {
                del.setEnabled(false);
                if (des != null) des.setEnabled(false);
                if (act != null) act.setEnabled(false);
            }
        });
        btns.add(add); btns.add(del);
        if (act != null) btns.add(act);
        if (des != null) btns.add(des);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    private Dimension computeMaxSize() {
        CardLayout cl = (CardLayout) pnlExtras.getLayout();
        Dimension max = new Dimension(minSizeBase);
        for (String card : new String[]{"VEST", "FRACC"}) {
            cl.show(pnlExtras, card);
            Dimension d = getPreferredSize();
            if (d.width > max.width) max.width = d.width;
            if (d.height > max.height) max.height = d.height;
        }
        cl.show(pnlExtras, "NONE");
        return max;
    }

    private JPanel buildPresentacionesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5,5));
        tblPresentaciones.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        panel.add(new JScrollPane(tblPresentaciones), BorderLayout.CENTER);
        if (tblPresentaciones.getColumnCount() > 0) {
            tblPresentaciones.getColumnModel().removeColumn(
                    tblPresentaciones.getColumnModel().getColumn(0));
        }
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0));
        JButton add = new JButton();
        add.setIcon(new FlatSVGIcon("com/comercialvalerio/presentation/ui/icon/svg/plus.svg"));
        KeyStroke addKey = KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.ALT_DOWN_MASK);
        KeyStroke addKey2 = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
                InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        KeyUtils.setTooltipAndMnemonic(add, "Añadir fila", addKey, addKey2);
        JButton del = new JButton();
        del.setIcon(new FlatSVGIcon("com/comercialvalerio/presentation/ui/icon/svg/minus.svg"));
        KeyStroke delKey = KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.ALT_DOWN_MASK);
        KeyStroke delKey2 = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.ALT_DOWN_MASK);
        KeyUtils.setTooltipAndMnemonic(del, "Quitar fila", delKey, delKey2);
        final JButton act = editing ? new JButton() : null;
        final JButton des = editing ? new JButton() : null;
        final KeyStroke actKey = editing ? KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK) : null;
        final KeyStroke desKey = editing ? KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK) : null;
        if (editing) {
            act.setIcon(new FlatSVGIcon("com/comercialvalerio/presentation/ui/icon/svg/lock_open.svg"));
            KeyUtils.setTooltipAndMnemonic(act, "Activar", actKey);
            des.setIcon(new FlatSVGIcon("com/comercialvalerio/presentation/ui/icon/svg/lock.svg"));
            KeyUtils.setTooltipAndMnemonic(des, "Desactivar", desKey);
        }
        del.setEnabled(false);
        if (des != null) des.setEnabled(false);
        if (act != null) act.setEnabled(false);
        add.addActionListener(e -> modelPres.addRow(new Object[]{null,"","","Activo"}));
        del.addActionListener(e -> {
            int r = tblPresentaciones.getSelectedRow();
            if (r>=0) modelPres.removeRow(r);
        });
        if (des != null) {
            des.addActionListener(e -> {
                int r = tblPresentaciones.getSelectedRow();
                if (r>=0 && controller != null) controller.desactivarPresentacion(this);
            });
        }
        if (act != null) {
            act.addActionListener(e -> {
                int r = tblPresentaciones.getSelectedRow();
                if (r>=0 && controller != null) controller.activarPresentacion(this);
            });
        }
        KeyUtils.registerKeyAction(getRootPane(),
                addKey,
                add::doClick);
        KeyUtils.registerKeyAction(getRootPane(),
                addKey2,
                add::doClick);
        KeyUtils.registerKeyAction(getRootPane(),
                delKey,
                del::doClick);
        KeyUtils.registerKeyAction(getRootPane(),
                delKey2,
                del::doClick);
        if (des != null) {
            KeyUtils.registerKeyAction(getRootPane(), desKey, des::doClick);
        }
        if (act != null) {
            KeyUtils.registerKeyAction(getRootPane(), actKey, act::doClick);
        }
        tblPresentaciones.getSelectionModel().addListSelectionListener(e -> {
            int r = tblPresentaciones.getSelectedRow();
            if (r >= 0) {
                del.setEnabled(true);
                if (editing) {
                    String est = modelPres.getValueAt(r,3).toString();
                    boolean activo = "Activo".equalsIgnoreCase(est);
                    if (des != null) des.setEnabled(activo);
                    if (act != null) act.setEnabled(!activo);
                }
            } else {
                del.setEnabled(false);
                if (des != null) des.setEnabled(false);
                if (act != null) act.setEnabled(false);
            }
        });
        btns.add(add); btns.add(del);
        if (act != null) btns.add(act);
        if (des != null) btns.add(des);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    public void showExtras(String tipoNombre) {
        CardLayout cl = (CardLayout) pnlExtras.getLayout();
        boolean disablePrice = false;
        boolean disableStock = false;
        boolean visible = false;
        if ("Vestimenta".equalsIgnoreCase(tipoNombre)) {
            cl.show(pnlExtras, "VEST");
            TableUtils.clearModel(modelPres);
            visible = true;
            disableStock = true;
        } else if ("Fraccionable".equalsIgnoreCase(tipoNombre)) {
            cl.show(pnlExtras, "FRACC");
            TableUtils.clearModel(modelTallas);
            visible = true;
            disablePrice = true;
        } else {
            cl.show(pnlExtras, "NONE");
            TableUtils.clearModel(modelTallas);
            TableUtils.clearModel(modelPres);
        }
        pnlExtras.setVisible(visible);
        if (visible) {
            pnlExtras.setPreferredSize(null);
            setMinimumSize(minSizeExtras);
        } else {
            pnlExtras.setPreferredSize(new Dimension(0, 0));
            setMinimumSize(minSizeBase);
        }
        txtPrecioUnitario.setEnabled(!disablePrice);
        txtStockActual.setEnabled(!disableStock);
        if (disablePrice) {
            txtPrecioUnitario.setText("");
        }
        if (disableStock) {
            txtStockActual.setText("");
        }
        if (isDisplayable()) {
            pack();
            DialogUtils.ensureMinWidth(this);
            setLocationRelativeTo(getOwner());
        }
    }

    private void updatePedidoFields() {
        boolean visible = chkParaPedido.isSelected();
        lblTipoPedido.setVisible(visible);
        cboTipoPedido.setVisible(visible);
        if (isDisplayable()) {
            pack();
            DialogUtils.ensureMinWidth(this);
        }
    }

    private void updateMayoristaFields() {
        boolean visible = chkMayorista.isSelected();
        lblMinMayorista.setVisible(visible);
        spnMinMayorista.setVisible(visible);
        lblPrecioMayorista.setVisible(visible);
        txtPrecioMayorista.setVisible(visible);
        if (isDisplayable()) {
            pack();
            DialogUtils.ensureMinWidth(this);
        }
    }

    private void attemptCancel() {
        if (!txtNombre.getText().isBlank()
                || !txtDescripcion.getText().isBlank()
                || !txtUnidad.getText().isBlank()
                || !txtPrecioUnitario.getText().isBlank()
                || !txtPrecioMayorista.getText().isBlank()
                || !txtStockActual.getText().isBlank()
                || !txtUmbral.getText().isBlank()
                || modelTallas.getRowCount() > 0
                || modelPres.getRowCount() > 0) {
            if (!DialogUtils.confirmAction(this, "¿Descartar cambios?"))
                return;
        }
        dispose();
    }

    public JTextField getTxtNombre() { return txtNombre; }
    public JTextArea  getTxtDescripcion() { return txtDescripcion; }
    public JComboBox<CategoriaDto> getCboCategoria() { return cboCategoria; }
    public JComboBox<TipoProductoDto> getCboTipo() { return cboTipo; }
    public JTextField getTxtUnidad() { return txtUnidad; }
    public JTextField getTxtPrecioUnitario() { return txtPrecioUnitario; }
    public JCheckBox getChkMayorista() { return chkMayorista; }
    public JCheckBox getChkParaPedido() { return chkParaPedido; }
    public JComboBox<TipoPedido> getCboTipoPedido() { return cboTipoPedido; }
    public JSpinner getSpnMinMayorista() { return spnMinMayorista; }
    public JTextField getTxtPrecioMayorista() { return txtPrecioMayorista; }
    public JLabel getLblStockInicial() { return lblStockInicial; }
    public JTextField getTxtStockActual() { return txtStockActual; }
    public JTextField getTxtUmbral() { return txtUmbral; }
    public JButton getBtnGuardar() { return btnGuardar; }
    public DefaultTableModel getModelTallas() { return modelTallas; }
    public DefaultTableModel getModelPresentaciones() { return modelPres; }
    public JTable getTblTallas() { return tblTallas; }
    public JTable getTblPresentaciones() { return tblPresentaciones; }
    public HeaderPanel getHeader() { return header; }
    public void setTallasBloqueadas(java.util.Set<Integer> ids) {
        this.tallasBloqueadas = ids == null ? java.util.Set.of() : ids;
    }

    /** Registra callbacks del controlador y la acción F5. */
    public void setController(ProductoController controller) {
        this.controller = controller;
        KeyUtils.setTooltipAndMnemonic(cboCategoria, KeyUtils.REFRESH_KEY, "Actualizar");
        KeyUtils.setTooltipAndMnemonic(cboTipo, KeyUtils.REFRESH_KEY, "Actualizar");
        KeyUtils.registerRefreshAction(getRootPane(), this::reloadCombos);
    }

    /** Recarga las listas de categoría y tipo usando el controlador. */
    public void reloadCombos() {
        if (controller != null) {
            controller.reloadCombos(this);
        }
    }

}

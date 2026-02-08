package com.comercialvalerio.presentation.ui.ventas;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;

import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.CurrencyUtils;
import com.comercialvalerio.presentation.ui.util.DigitFilter;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.NumericFilter;
import com.comercialvalerio.presentation.ui.util.SearchField;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

public class FormVenta extends BaseForm implements Refreshable {

    /* widgets reutilizables */
    private final SearchField searchField = new SearchField("Buscar producto");
    private final JTable     tblStock    = new NonEditableTable();
    private final JTable     tblAdded    = new NonEditableTable();
    private final JScrollPane spStock    = new JScrollPane(tblStock);
    private final JScrollPane spAdded    = new JScrollPane(tblAdded);
    private final JTextField txtCantidad = new JTextField(6);

    private final JTextField txtCliNom   = new JTextField(15);
    private final JTextField txtCliTel   = new JTextField(12);
    private final JTable     tblClientes = new NonEditableTable();
    private final JScrollPane spClientes = new JScrollPane(tblClientes);

    private final JLabel lblStockEmpty   = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JLabel lblAddedEmpty   = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JLabel lblClientesEmpty = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);

    private final JCheckBox  chkDigital  = new JCheckBox("Billetera Digital");
    private final JTextField txtDigital  = new JTextField();
    private final JCheckBox  chkEfectivo = new JCheckBox("Efectivo");
    private final JTextField txtEfectivo = new JTextField();

    private final JLabel lblSubTotal =
            new JLabel(CurrencyUtils.format(BigDecimal.ZERO));
    private final JLabel lblTotal    =
            new JLabel(CurrencyUtils.format(BigDecimal.ZERO));

    /** Temporizador usado para la búsqueda diferida de productos. */
    private javax.swing.Timer searchTimer;

    /* botones principales */
    private JButton btnAdd;
    private JButton btnRemove;
    private JButton btnObs;
    private JButton btnRegCli;
    private JButton btnComprobante;
    private JButton btnCancelar;
    private JButton btnRefresh;

    private final com.comercialvalerio.presentation.controller.ventas.VentaController controller;

    public FormVenta() {
        buildUI();
        ((AbstractDocument) txtCantidad.getDocument())
                .setDocumentFilter(new DigitFilter(DbConstraints.CANTIDAD_INTEGER,
                                                txtCantidad));
        ((AbstractDocument) txtDigital.getDocument())
                .setDocumentFilter(new NumericFilter(txtDigital));
        ((AbstractDocument) txtEfectivo.getDocument())
                .setDocumentFilter(new NumericFilter(txtEfectivo));
        controller = new com.comercialvalerio.presentation.controller.ventas.VentaController(this);
        controller.cargarProductos();
        controller.cargarClientes();

        searchTimer = new javax.swing.Timer(300, e -> controller.cargarProductos());
        searchTimer.setRepeats(false);
        javax.swing.event.DocumentListener searchListener = new javax.swing.event.DocumentListener() {
            private void restart() { searchTimer.restart(); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { restart(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { restart(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { restart(); }
        };
        searchField.getTextField().getDocument().addDocumentListener(searchListener);
        searchField.getTextField().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { searchTimer.stop(); }
        });
        searchField.addActionListener(e -> {
            searchTimer.stop();
            controller.cargarProductos();
        });
        searchField.addClearActionListener(e -> {
            searchTimer.stop();
            searchField.getTextField().setText("");
            controller.cargarProductos();
        });

        btnAdd.addActionListener(e -> {
            searchTimer.stop();
            controller.agregarDetalle();
        });
        btnRemove.addActionListener(e -> {
            searchTimer.stop();
            controller.quitarDetalle();
        });
        btnComprobante.addActionListener(e -> controller.crear());
        btnCancelar.addActionListener(e -> controller.cancelar());
        btnObs.addActionListener(e -> controller.abrirObservacion());
        btnRegCli.addActionListener(e -> controller.registrarCliente());
        tblClientes.getSelectionModel().addListSelectionListener(e -> controller.seleccionarCliente());
        chkDigital.addActionListener(e -> controller.updatePagoFields());
        chkEfectivo.addActionListener(e -> controller.updatePagoFields());
        txtDigital.setEnabled(false);
        txtEfectivo.setEnabled(false);
        TableUtils.updateEmptyView(spAdded, tblAdded, lblAddedEmpty);
        lblTotal.addPropertyChangeListener("text", e -> controller.updatePagoFields());
    }

    private void buildUI() {
        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]", "[]8[grow]8[]8[]push"));
        setOpaque(false);
        putClientProperty(FlatClientProperties.STYLE, "background:@background");

                /* ---------- encabezado ----------------------------------------------- */
        btnRefresh = UIUtils.createRefreshButton(this::refresh);
        HeaderPanel header = new HeaderPanel("Venta", btnRefresh);
        header.setBorder(new EmptyBorder(0,0,5,0));
        add(header, "cell 0 0, growx, wrap");

        /* ---------- cuerpo principal --------------------------------------------- */
        JPanel body = new JPanel(new MigLayout(
                "insets 0, gap 8, fill",
                "[grow,fill][2!][grow,fill][2!][250!,fill]",
                "[]8[grow]8[]8[]push"));
        body.setOpaque(false);
        add(body, "cell 0 1, grow");

        /* ---------- fila 0: búsqueda --------------------------------------------- */
        body.add(searchField, "cell 0 0, split 2, growx 50, h 40!");
        body.add(Box.createHorizontalStrut(0), "growx, push, wrap");

        /* ---------- fila 1: tablas y panel derecho ------------------------------- */
        body.add(spStock, "cell 0 1, grow, sgx tables");
        body.add(new JSeparator(SwingConstants.VERTICAL), "cell 1 1, growy");
        body.add(spAdded, "cell 2 1, grow, sgx tables");
        body.add(new JSeparator(SwingConstants.VERTICAL), "cell 3 1, growy");
        JPanel right = buildRightPanel();
        body.add(right, "cell 4 1, growy");

        /* ---------- fila 2: Cantidad + Añadir ------------------------------------ */
        JLabel lblCant = new JLabel("Cantidad");
        body.add(lblCant, "cell 0 2, split 3, aligny center");
        txtCantidad.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "1");
        txtCantidad.setText("1");
        body.add(txtCantidad, "w 60!, gapleft 5, aligny center");
        btnAdd = buildChip(
                "Añadir",
                UIStyle.RGB_ACTION_GREEN,
                "com/comercialvalerio/presentation/ui/icon/svg/plus.svg");
        KeyUtils.setTooltipAndMnemonic(btnAdd, KeyEvent.VK_A, "Añadir");
        body.add(btnAdd, "gapleft 5, aligny center");

        btnRemove = buildChip(
                "Quitar",
                UIStyle.RGB_ACTION_RED,
                "com/comercialvalerio/presentation/ui/icon/svg/minus.svg");
        KeyUtils.setTooltipAndMnemonic(btnRemove, KeyEvent.VK_Q, "Quitar");
        body.add(btnRemove, "cell 2 2, aligny center, wrap");

        /* ---------- fila 3: SubTotal / Total -------------------------------------- */
        JPanel totals = new JPanel(new GridLayout(2,2,5,0));
        totals.setOpaque(false);
        totals.add(new JLabel("Sub total"));
        totals.add(lblSubTotal);
        totals.add(new JLabel("Total"));
        totals.add(lblTotal);
        totals.setBorder(new EmptyBorder(6,6,6,6));
        body.add(totals, "cell 0 3 3 1, growx");

        /* ---------- borde general ------------------------------------------------- */
        setBorder(UIStyle.FORM_BORDER_GRAY);
    }

    /* ---------------- panel derecho (cliente, pago, botones) ----------------------- */
    private JPanel buildRightPanel() {
        JPanel r = new JPanel(new MigLayout(
                "fillx, wrap, insets 0", "[fill]", "[]10[]10[grow]5[]5[]15[]"));
        r.setOpaque(false);

        JLabel lblCli = new JLabel("Detalles del Cliente", SwingConstants.CENTER);
        lblCli.putClientProperty(FlatClientProperties.STYLE,"font:$h2.font");
        r.add(lblCli, "growx, alignx center");

        txtCliNom.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,"Nombre");
        txtCliNom.setEditable(false);

        txtCliTel.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,"Teléfono");
        txtCliTel.setEditable(false);

        r.add(txtCliNom, "split 2");
        r.add(txtCliTel, "wrap");

        // Se aumenta la altura para facilitar la selección de clientes
        spClientes.setPreferredSize(new Dimension(220,220));
        r.add(spClientes, "growx");
        r.add(createSeparator(), "growx, gapy 5");

        /* botón verde Registrar Cliente */
        btnRegCli = buildAction(
                "Registrar Cliente",
                UIStyle.RGB_ACTION_GREEN,
                "com/comercialvalerio/presentation/ui/icon/svg/plus.svg");
        KeyUtils.setTooltipAndMnemonic(btnRegCli, KeyEvent.VK_N, "Registrar Cliente");
        r.add(btnRegCli, "growx");
        r.add(createSeparator(), "growx, gapy 5");

        JLabel lblPago = new JLabel("Método de Pago");
        lblPago.putClientProperty(FlatClientProperties.STYLE,"font:$h2.font");
        r.add(lblPago, "gapbottom 5");

        JPanel pagos = new JPanel(new MigLayout("insets 0, gap 5", "[fill][grow]", "[]5[]"));
        pagos.setOpaque(false);
        pagos.add(chkDigital);
        pagos.add(txtDigital, "growx, wrap");
        pagos.add(chkEfectivo);
        pagos.add(txtEfectivo, "growx");
        r.add(pagos, "growx");

        btnObs = buildAction("Añadir Observación", UIStyle.RGB_ACTION_PURPLE,
                "com/comercialvalerio/presentation/ui/icon/svg/edit.svg");
        KeyUtils.setTooltipAndMnemonic(btnObs, KeyEvent.VK_O, "Añadir Observación");
        btnComprobante = buildAction("Generar Comprobante", UIStyle.RGB_ACTION_BLUE,
                "com/comercialvalerio/presentation/ui/icon/svg/printer.svg");
        KeyUtils.setTooltipAndMnemonic(btnComprobante, KeyEvent.VK_G, "Generar Comprobante");
        btnCancelar = buildAction("Cancelar", UIStyle.RGB_ACTION_RED,
                "com/comercialvalerio/presentation/ui/icon/svg/close_circle.svg");
        KeyUtils.setTooltipAndMnemonic(btnCancelar, KeyEvent.VK_C, "Cancelar");
        r.add(createSeparator(), "growx, gapy 5");
        r.add(btnObs,"growx");
        r.add(createSeparator(), "growx, gapy 5");
        r.add(btnComprobante,"growx");
        r.add(btnCancelar,"growx");

        return r;
    }

    private JButton buildAction(String txt, int rgb, String svg) {
        JButton b = new JButton(txt);
        ButtonStyles.styleAction(b, rgb, svg);
        return b;
    }

    private JButton buildChip(String txt, int rgb, String svg) {
        JButton b = new JButton(txt);
        ButtonStyles.styleAction(b, rgb, svg);
        return b;
    }

    private JSeparator createSeparator() {
        JSeparator s = new JSeparator();
        s.setForeground(UIStyle.COLOR_BORDER_GRAY);
        return s;
    }

    /* ========= getters para el controlador ========= */
    public JTextField getTxtBuscar()   { return searchField.getTextField(); }
    public JTable     getTblStock()    { return tblStock; }
    public JTable     getTblAdded()    { return tblAdded; }
    public JScrollPane getSpStock()    { return spStock; }
    public JScrollPane getSpAdded()    { return spAdded; }
    public JTextField getTxtCantidad() { return txtCantidad; }
    public JTextField getTxtCliNom()   { return txtCliNom; }
    public JTextField getTxtCliTel()   { return txtCliTel; }
    public JTable     getTblClientes() { return tblClientes; }
    public JScrollPane getSpClientes() { return spClientes; }
    public JLabel     getLblStockEmpty() { return lblStockEmpty; }
    public JLabel     getLblAddedEmpty() { return lblAddedEmpty; }
    public JLabel     getLblClientesEmpty() { return lblClientesEmpty; }
    public JCheckBox  getChkDigital()  { return chkDigital; }
    public JTextField getTxtDigital()  { return txtDigital; }
    public JCheckBox  getChkEfectivo() { return chkEfectivo; }
    public JTextField getTxtEfectivo() { return txtEfectivo; }
    public JLabel     getLblSubTotal() { return lblSubTotal; }
    public JLabel     getLblTotal()    { return lblTotal; }
    public JButton    getBtnAdd()      { return btnAdd; }
    public JButton    getBtnRemove()   { return btnRemove; }
    public JButton    getBtnObs()      { return btnObs; }
    public JButton    getBtnRegCli()   { return btnRegCli; }
    public JButton    getBtnComprobante() { return btnComprobante; }
    public JButton    getBtnCancelar() { return btnCancelar; }

    @Override
    public void refresh() {
        controller.cargarProductos();
        controller.cargarClientes();
    }

    @Override
    protected void registerShortcuts() {
        KeyUtils.registerRefreshAction(this, () -> {
            controller.cargarProductos();
            controller.cargarClientes();
        });
        KeyUtils.registerFocusAction(this, searchField.getTextField());
    }
}

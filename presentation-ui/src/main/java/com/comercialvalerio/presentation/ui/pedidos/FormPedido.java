package com.comercialvalerio.presentation.ui.pedidos;

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
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.CurrencyUtils;
import com.comercialvalerio.presentation.ui.util.DigitFilter;
import com.comercialvalerio.presentation.ui.util.DocumentListeners;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.SearchField;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

/**
 * Formulario para registrar Pedidos Especiales.
 * Basado en el diseño provisto y en la estructura de FormVenta.
 */
public class FormPedido extends BaseForm implements Refreshable {

    /* widgets reutilizables */
    private final SearchField searchField = new SearchField("Buscar producto");
    private final JTable     tblStock    = new NonEditableTable();
    private final JTable     tblAdded    = new NonEditableTable();
    private final JScrollPane spStock    = new JScrollPane(tblStock);
    private final JScrollPane spAdded    = new JScrollPane(tblAdded);
    private final JLabel lblEmptyStock   = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JLabel lblEmptyAdded   = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JTextField txtCantidad = new JTextField(6);

    private final JTextField txtCliNom   = new JTextField(15);
    private final JTextField txtCliTel   = new JTextField(12);
    private final JTextField txtCliDir   = new JTextField(15);
    private final JTable     tblClientes = new NonEditableTable();
    private final JScrollPane spClientes = new JScrollPane(tblClientes);
    private final JLabel lblEmptyClientes = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);

    private final JLabel lblSubTotal =
            new JLabel(CurrencyUtils.format(BigDecimal.ZERO));
    private final JLabel lblCargo    =
            new JLabel(CurrencyUtils.format(BigDecimal.ZERO));
    private final JLabel lblTotal    =
            new JLabel(CurrencyUtils.format(BigDecimal.ZERO));
    private final JPanel subTotalPanel = new JPanel(new GridLayout(1,2,5,0));
    private final JPanel cargoPanel  = new JPanel(new GridLayout(1,2,5,0));
    private final JLabel lblTotalOvillos = new JLabel("0");
    private final JCheckBox chkValeGas = new JCheckBox("Vale de Gas");

    private JButton btnAdd;
    private JButton btnQuitar;
    private JButton btnObs;
    private JButton btnRegCli;
    private JButton btnRegistrar;
    private JButton btnCancelar;
    private JButton btnRefresh;
    protected final boolean domicilio;
    private com.comercialvalerio.presentation.controller.pedidos.PedidoController controller;

    public FormPedido() { this(false); }

    public FormPedido(boolean domicilio) {
        this.domicilio = domicilio;
        buildUI();
        ((AbstractDocument) txtCantidad.getDocument())
                .setDocumentFilter(new DigitFilter(DbConstraints.CANTIDAD_INTEGER, txtCantidad));
        chkValeGas.setVisible(false);
    }

    /**
     * Inicializa el controlador y registra todos los listeners de eventos.
     * Debe invocarse desde las subclases después del constructor.
     */
    protected void initController(
            com.comercialvalerio.presentation.controller.pedidos.PedidoController controller) {
        this.controller = controller;
        controller.cargarProductos();
        controller.cargarClientes();
        searchField.addActionListener(e -> controller.cargarProductos());
        DocumentListeners.attachDebounced(searchField.getTextField(), controller::cargarProductos);
        searchField.addClearActionListener(e -> {
            searchField.getTextField().setText("");
            controller.cargarProductos();
        });
        btnAdd.addActionListener(e -> controller.agregarDetalle());
        btnQuitar.addActionListener(e -> controller.quitarDetalle());
        btnRegistrar.addActionListener(e -> controller.crear());
        btnCancelar.addActionListener(e -> controller.cancelar());
        btnRegCli.addActionListener(e -> controller.registrarCliente());
        tblClientes.getSelectionModel().addListSelectionListener(e -> controller.seleccionarCliente());
        tblStock.getSelectionModel().addListSelectionListener(e -> controller.seleccionarProducto());
        btnObs.addActionListener(e -> controller.abrirObservacion());
        chkValeGas.addActionListener(e -> controller.refrescarTotales());
        controller.refrescarTotales();
    }

    private void buildUI() {
        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]", "[]8[grow]"));
        setOpaque(false);
        putClientProperty(FlatClientProperties.STYLE,"background:@background");

        /* ---------- encabezado ---------------------------------------------------- */
        btnRefresh = UIUtils.createRefreshButton(this::refresh);
        HeaderPanel header = new HeaderPanel(
                domicilio ? "Pedido Domicilio" : "Pedido Especial",
                btnRefresh);
        header.setBorder(new EmptyBorder(0,0,5,0));
        add(header, "cell 0 0, growx, wrap");

        /* ---------- cuerpo principal --------------------------------------------- */
        JPanel body = new JPanel(new MigLayout(
                "insets 0, gap 8, fill",
                "[grow,fill][2!][grow,fill][2!][250!,fill]",
                "[]8[grow]8[]8[]push"));
        body.setOpaque(false);
        add(body, "cell 0 1, grow");

        /* fila 0: buscador -------------------------------------------------------- */
        body.add(searchField,"cell 0 0, split 2, growx 50, h 40!");
        body.add(Box.createHorizontalStrut(0), "growx, push, wrap");

        /* fila 1: tablas + panel derecho ----------------------------------------- */
        body.add(spStock,"cell 0 1, grow, sgx tables");
        body.add(new JSeparator(SwingConstants.VERTICAL),"cell 1 1, growy");
        body.add(spAdded,"cell 2 1, grow, sgx tables");
        body.add(new JSeparator(SwingConstants.VERTICAL),"cell 3 1, growy");
        body.add(buildRightPanel(),"cell 4 1, growy");

        /* fila 2: Cantidad + Añadir ---------------------------------------------- */
        body.add(new JLabel("Cantidad"), "cell 0 2, split 3, aligny center");
        txtCantidad.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "1");
        txtCantidad.setText("1");
        body.add(txtCantidad, "w 60!, gapleft 5, aligny center");
        btnAdd = buildChip(
                "Añadir",
                UIStyle.RGB_ACTION_GREEN,
                "com/comercialvalerio/presentation/ui/icon/svg/plus.svg");
        body.add(btnAdd, "gapleft 5, aligny center");
        KeyUtils.setTooltipAndMnemonic(btnAdd, KeyEvent.VK_A, "Añadir");

        btnQuitar = buildChip(
                "Quitar",
                UIStyle.RGB_ACTION_RED,
                "com/comercialvalerio/presentation/ui/icon/svg/minus.svg");
        KeyUtils.setTooltipAndMnemonic(btnQuitar, KeyEvent.VK_Q, "Quitar");
        body.add(btnQuitar, "cell 2 2, aligny center, wrap");

        /* fila 3: Subtotal / Total ------------------------------------------------ */
        JPanel totals = new JPanel(new GridLayout(4,1,0,0));
        totals.setOpaque(false);

        JPanel ovillosRow = new JPanel(new GridLayout(1,2,5,0));
        ovillosRow.setOpaque(false);
        ovillosRow.add(new JLabel("Ovillos"));
        ovillosRow.add(lblTotalOvillos);
        if (!domicilio) {
            totals.add(ovillosRow);
        }

        subTotalPanel.setOpaque(false);
        subTotalPanel.add(new JLabel("Sub total"));
        subTotalPanel.add(lblSubTotal);
        totals.add(subTotalPanel);

        cargoPanel.setOpaque(false);
        cargoPanel.add(new JLabel("Cargo"));
        cargoPanel.add(lblCargo);
        totals.add(cargoPanel);

        JPanel totalRow = new JPanel(new GridLayout(1,2,5,0));
        totalRow.setOpaque(false);
        totalRow.add(new JLabel("Total"));
        totalRow.add(lblTotal);
        totals.add(totalRow);
        totals.setBorder(new EmptyBorder(6,6,6,6));
        body.add(totals,"cell 0 3 3 1, growx");

        /* borde general ----------------------------------------------------------- */
        setBorder(UIStyle.FORM_BORDER_GRAY);
    }

    /* ---------------- panel derecho (cliente + botones) ------------------------- */
    private JPanel buildRightPanel() {
        JPanel r = new JPanel(new MigLayout(
                "fillx, wrap, insets 0","[fill]",
                "[]10[]10[]10[grow]5[]15[]5[]5[]"));
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

        txtCliDir.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,"Dirección");
        txtCliDir.setEditable(false);
        r.add(txtCliDir, "wrap");

        // Se incrementa la altura para mostrar más clientes en pantalla
        spClientes.setPreferredSize(new Dimension(220,220));
        r.add(spClientes,"growx");
        r.add(new JSeparator(), "growx, gapy 5");

        /* botón verde registrar cliente */
        btnRegCli = buildAction(
                "Registrar Cliente",
                UIStyle.RGB_ACTION_GREEN,
                "com/comercialvalerio/presentation/ui/icon/svg/plus.svg");
        r.add(btnRegCli,"growx");
        r.add(new JSeparator(), "growx, gapy 5");

        /* uso de vale de gas */
        r.add(chkValeGas, "growx");

        KeyUtils.setTooltipAndMnemonic(btnRegCli, KeyEvent.VK_N, "Registrar Cliente");
        /* separador */
        r.add(new JSeparator(),"growx, gapy 8");

        /* botones finales */
        btnObs = buildAction("Añadir Observación", UIStyle.RGB_ACTION_PURPLE,
                "com/comercialvalerio/presentation/ui/icon/svg/edit.svg");
        KeyUtils.setTooltipAndMnemonic(btnObs, KeyEvent.VK_O, "Añadir Observación");
        r.add(btnObs, "growx");
        r.add(new JSeparator(), "growx, gapy 5");

        btnRegistrar = buildAction(
                domicilio ? "Registrar Pedido" : "Registrar Pedido Especial",
                UIStyle.RGB_ACTION_BLUE,
                "com/comercialvalerio/presentation/ui/icon/svg/upload.svg");
        KeyUtils.setTooltipAndMnemonic(btnRegistrar, KeyEvent.VK_R,
                domicilio ? "Registrar Pedido" : "Registrar Pedido Especial");
        r.add(btnRegistrar, "growx");

        btnCancelar = buildAction("Cancelar", UIStyle.RGB_ACTION_RED,
                "com/comercialvalerio/presentation/ui/icon/svg/close_circle.svg");
        KeyUtils.setTooltipAndMnemonic(btnCancelar, KeyEvent.VK_C, "Cancelar");
        r.add(btnCancelar, "growx");

        return r;
    }

    /* ---------- auxiliares ------------------------------------------------------- */
    private JButton buildChip(String txt,int rgb,String svg){
        JButton b = new JButton(txt);
        ButtonStyles.styleAction(b, rgb, svg);
        return b;
    }

    private JButton buildAction(String txt,int rgb){ return buildAction(txt,rgb,null); }
    private JButton buildAction(String txt,int rgb,String svg){
        JButton b=new JButton(txt);
        ButtonStyles.styleBottom(b, rgb, svg);
        return b;
    }

    /* -------- getters para el controlador -------- */
    public JTextField getTxtBuscar() { return searchField.getTextField(); }
    public JTable getTblStock() { return tblStock; }
    public JScrollPane getSpStock() { return spStock; }
    public JLabel getLblEmptyStock() { return lblEmptyStock; }
    public JTable getTblAdded() { return tblAdded; }
    public JScrollPane getSpAdded() { return spAdded; }
    public JLabel getLblEmptyAdded() { return lblEmptyAdded; }
    public JTextField getTxtCantidad() { return txtCantidad; }
    public JTextField getTxtCliNom() { return txtCliNom; }
    public JTextField getTxtCliTel() { return txtCliTel; }
    public JTextField getTxtCliDir() { return txtCliDir; }
    public JTable getTblClientes() { return tblClientes; }
    public JScrollPane getSpClientes() { return spClientes; }
    public JLabel getLblEmptyClientes() { return lblEmptyClientes; }
    public JLabel getLblSubTotal() { return lblSubTotal; }
    public JLabel getLblCargo()    { return lblCargo; }
    public JLabel getLblTotalOvillos() { return lblTotalOvillos; }
    public JLabel getLblTotal() { return lblTotal; }
    public JPanel getSubTotalPanel() { return subTotalPanel; }
    public JPanel getCargoPanel() { return cargoPanel; }
    public JCheckBox getChkValeGas() { return chkValeGas; }
    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnObs() { return btnObs; }
    public JButton getBtnRegCli() { return btnRegCli; }
    public JButton getBtnRegistrar() { return btnRegistrar; }
    public JButton getBtnCancelar() { return btnCancelar; }
    public JButton getBtnQuitar() { return btnQuitar; }
    /** Devuelve {@code true} cuando el formulario es para pedidos a domicilio. */
    public boolean isDomicilio() { return domicilio; }

    @Override
    public void refresh() {
        if (controller != null) {
            controller.cargarProductos();
            controller.cargarClientes();
        }
    }

    @Override
    protected void registerShortcuts() {
        if (controller != null) {
            KeyUtils.registerRefreshAction(this, () -> {
                controller.cargarProductos();
                controller.cargarClientes();
            });
            KeyUtils.registerFocusAction(this, searchField.getTextField());
        }
    }
}

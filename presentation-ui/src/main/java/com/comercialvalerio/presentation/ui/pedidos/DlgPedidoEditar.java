package com.comercialvalerio.presentation.ui.pedidos;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JCheckBox;
import javax.swing.AbstractAction;
import com.comercialvalerio.presentation.ui.base.BaseDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;

import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.DigitFilter;
import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.SearchField;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.ui.util.CurrencyUtils;
import java.math.BigDecimal;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import net.miginfocom.swing.MigLayout;

/** Diálogo para editar un pedido existente.
 *  Utiliza el mismo diseño de selección de productos que {@link FormPedido}. */
public class DlgPedidoEditar extends BaseDialog {

    private final SearchField searchField = new SearchField("Buscar producto");
    private final JLabel lblOvillosTitle = new JLabel("Ovillos");
    private final JLabel lblCargoTitle   = new JLabel("Cargo");
    private final JTable     tblStock    = new NonEditableTable();
    private final JTable     tblAdded    = new NonEditableTable();
    private final JScrollPane spStock    = new JScrollPane(tblStock);
    private final JScrollPane spAdded    = new JScrollPane(tblAdded);
    private final JLabel lblEmptyStock  = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JLabel lblEmptyAdded  = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JTextField txtCantidad = new JTextField(6);
    private final JCheckBox  chkValeGas  = new JCheckBox("Vale de Gas");
    private final JLabel     lblSubTitle = new JLabel("Sub total");
    private final JLabel     lblSubTotal =
            new JLabel(CurrencyUtils.format(BigDecimal.ZERO));
    private final JLabel     lblCargo    =
            new JLabel(CurrencyUtils.format(BigDecimal.ZERO));
    private final JLabel     lblTotal    =
            new JLabel(CurrencyUtils.format(BigDecimal.ZERO));
    private final JLabel     lblTotalOvillos = new JLabel("0");
    private final JButton    btnAdd      = new JButton();
    private final JButton    btnRemove   = new JButton();
    private final JButton    btnGuardar;

    public DlgPedidoEditar(Window owner, boolean domicilio) {
        super(owner,
                domicilio ? "Editar Pedido" : "Editar Pedido Especial",
                ModalityType.APPLICATION_MODAL, new JButton("Guardar"));
        this.btnGuardar = getDefaultButton();
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptCancel();
            }
        });
        buildUI();
        KeyUtils.registerFocusAction(getRootPane(), searchField.getTextField());
        if (!domicilio) {
            lblSubTitle.setVisible(false);
            lblSubTotal.setVisible(false);
            chkValeGas.setVisible(false);
            lblCargoTitle.setVisible(false);
            lblCargo.setVisible(false);
        } else {
            lblOvillosTitle.setVisible(false);
            lblTotalOvillos.setVisible(false);
        }
        SwingUtilities.invokeLater(() -> searchField.getTextField().requestFocusInWindow());
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptCancel();
            }
        });
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.putClientProperty(FlatClientProperties.STYLE, "background:@background");
        root.setBorder(UIStyle.FORM_BORDER_VIOLET);
        setContentPane(root);

        /* ---------- centro: buscador, stock y artículos seleccionados --- */
        JPanel center = new JPanel(new MigLayout(
                "insets 0, gap 8, fill",
                "[grow,fill][2!][grow,fill]",
                "[]8[grow]8[]"));
        center.setOpaque(false);
        center.add(searchField, "cell 0 0, growx, h 40!, wrap");

        center.add(spStock, "cell 0 1, grow, sgx tables");
        center.add(new JSeparator(SwingConstants.VERTICAL), "cell 1 1, growy");
        center.add(spAdded, "cell 2 1, grow, sgx tables");

        center.add(new JLabel("Cantidad"), "cell 0 2, split 3, aligny center");
        ((AbstractDocument) txtCantidad.getDocument())
                .setDocumentFilter(new DigitFilter(DbConstraints.CANTIDAD_INTEGER,
                                                txtCantidad));
        txtCantidad.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "1");
        txtCantidad.setText("1");
        center.add(txtCantidad, "w 60!, gapleft 5, aligny center");
        btnAdd.setText("Añadir");
        btnAdd.setIcon(new FlatSVGIcon(
                "com/comercialvalerio/presentation/ui/icon/svg/plus.svg"));
        btnAdd.putClientProperty(FlatClientProperties.STYLE,
                "arc:" + UIStyle.ARC_ROUND + "; background:rgb(66,160,66); foreground:rgb(255,255,255)");
        KeyUtils.setTooltipAndMnemonic(btnAdd, KeyEvent.VK_A, "Añadir");
        center.add(btnAdd, "gapleft 5, aligny center");
        btnRemove.setText("Quitar");
        btnRemove.setIcon(new FlatSVGIcon(
                "com/comercialvalerio/presentation/ui/icon/svg/minus.svg"));
        btnRemove.putClientProperty(FlatClientProperties.STYLE,
                "arc:" + UIStyle.ARC_ROUND + "; background:rgb(231,76,60); foreground:rgb(255,255,255)");
        KeyUtils.setTooltipAndMnemonic(btnRemove, KeyEvent.VK_Q, "Quitar");
        center.add(btnRemove, "cell 2 2, aligny center");
        root.add(center, BorderLayout.CENTER);

        /* ---------- parte inferior: totales y opciones ------------------ */
        JPanel bottom = new JPanel(new MigLayout("fillx, insets 0, gap 8", "[grow]", "[]8[]8[]"));
        bottom.setOpaque(false);

        JPanel totals = new JPanel(new GridLayout(4,2,5,0));
        totals.setOpaque(false);
        totals.add(lblOvillosTitle);
        totals.add(lblTotalOvillos);
        totals.add(lblSubTitle);
        totals.add(lblSubTotal);
        totals.add(lblCargoTitle);
        totals.add(lblCargo);
        totals.add(new JLabel("Total"));
        totals.add(lblTotal);
        totals.setBorder(new EmptyBorder(6,6,6,6));
        bottom.add(totals, "growx, wrap");

        bottom.add(chkValeGas, "growx, wrap");

        ButtonStyles.styleBottom(btnGuardar, UIStyle.RGB_ACTION_GREEN,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        KeyUtils.setTooltipAndMnemonic(btnGuardar, KeyEvent.VK_G, "Guardar");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnGuardar);
        bottom.add(btnPanel, "growx");
        root.add(bottom, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnGuardar);
    }

    private void attemptCancel() {
        if (tblAdded.getRowCount() > 0
                || !searchField.getTextField().getText().isBlank()
                || !txtCantidad.getText().isBlank()) {
            if (!DialogUtils.confirmAction(this, "¿Descartar cambios?"))
                return;
        }
        dispose();
    }

    public JTextField getTxtBuscar()   { return searchField.getTextField(); }
    public JTable     getTblStock()    { return tblStock; }
    public JScrollPane getSpStock()    { return spStock; }
    public JLabel     getLblEmptyStock(){ return lblEmptyStock; }
    public JTable     getTblAdded()    { return tblAdded; }
    public JScrollPane getSpAdded()    { return spAdded; }
    public JLabel     getLblEmptyAdded(){ return lblEmptyAdded; }
    public JTextField getTxtCantidad() { return txtCantidad; }
    public JCheckBox  getChkValeGas()  { return chkValeGas; }
    public JLabel     getLblSubTotal() { return lblSubTotal; }
    public JLabel     getLblCargo()    { return lblCargo; }
    public JLabel     getLblTotalOvillos() { return lblTotalOvillos; }
    public JLabel     getLblTotal()    { return lblTotal; }
    public JButton    getBtnAdd()      { return btnAdd; }
    public JButton    getBtnRemove()   { return btnRemove; }
    public JButton    getBtnGuardar()  { return btnGuardar; }

    /** Añade un listener que se dispara al presionar Enter en el buscador. */
    public void addSearchActionListener(java.awt.event.ActionListener l) {
        searchField.addActionListener(l);
    }

}

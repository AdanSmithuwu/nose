package com.comercialvalerio.presentation.ui.pedidos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.comercialvalerio.presentation.ui.base.BaseDialog;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

/** Dialogo que muestra la informacion completa de un Pedido. */
public class DlgPedidoDetalle extends BaseDialog {

    private final JLabel lblId        = new JLabel();
    private final JLabel lblFecha     = new JLabel();
    private final JLabel lblEntrega   = new JLabel();
    private final JLabel lblDireccion = new JLabel();
    private final JLabel lblTipo      = new JLabel();
    private final JLabel lblValeGas   = new JLabel();
    private final JLabel lblEmpleado  = new JLabel();
    private final JLabel lblCliente   = new JLabel();
    private final JLabel lblTotal     = new JLabel();
    private final JLabel lblEstado    = new JLabel();
    /** Área de texto para el comentario de cancelación. */
    private final JTextArea lblComentario= new JTextArea(3, 30);
    private final JTable tblDetalles  = new NonEditableTable();
    private final JTable tblPagos     = new NonEditableTable();
    private final JScrollPane spDetalles = new JScrollPane(tblDetalles);
    private final JScrollPane spPagos    = new JScrollPane(tblPagos);
    private final JLabel lblEmptyDetalles = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JLabel lblEmptyPagos    = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JButton btnCerrar;

    public DlgPedidoDetalle(Window owner) {
        super(owner, "Detalle de Pedido", ModalityType.APPLICATION_MODAL, new JButton("Cerrar"));
        this.btnCerrar = getDefaultButton();
        buildUI();
        SwingUtilities.invokeLater(() -> btnCerrar.requestFocusInWindow());
        pack();
        Dimension size = getSize();
        size.height += 5; // ligeramente más alto
        setSize(size);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.putClientProperty(FlatClientProperties.STYLE, "background:@background");
        root.setBackground(UIStyle.getColorCardBg());
        root.setBorder(new EmptyBorder(20,20,20,20));
        setContentPane(root);

        // El diálogo ya tiene una barra de título nativa, evitar duplicarla aquí
        // o agregar un botón de cierre personalizado.

        JPanel body = new JPanel(new MigLayout("fillx, wrap 2, insets 0, gap 8","[right][grow,fill]",""));
        body.setOpaque(false);
        root.add(body, BorderLayout.CENTER);

        body.add(new JLabel("ID:"));          body.add(lblId);
        body.add(new JLabel("Fecha:"));       body.add(lblFecha);
        body.add(new JLabel("Entrega:"));     body.add(lblEntrega);
        body.add(new JLabel("Dirección:"));   body.add(lblDireccion);
        body.add(new JLabel("Tipo:"));        body.add(lblTipo);
        body.add(new JLabel("Vale Gas:"));    body.add(lblValeGas);
        body.add(new JLabel("Empleado:"));    body.add(lblEmpleado);
        body.add(new JLabel("Cliente:"));     body.add(lblCliente);
        body.add(new JLabel("Total:"));       body.add(lblTotal);
        body.add(new JLabel("Estado:"));      body.add(lblEstado);
        lblComentario.setLineWrap(true);
        lblComentario.setWrapStyleWord(true);
        lblComentario.setEditable(false);
        lblComentario.setOpaque(false);
        lblComentario.setBorder(null);
        lblComentario.setFocusable(false);
        body.add(new JLabel("Comentario:"), "top");
        body.add(lblComentario, "growx");

        body.add(new JLabel("Detalles"), "span 2, gaptop 10");
        tblDetalles.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        body.add(spDetalles, "span 2, grow, h 120!");

        body.add(new JLabel("Pagos"), "span 2, gaptop 10");
        tblPagos.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        body.add(spPagos, "span 2, grow, h 80!");

        ButtonStyles.styleBottom(btnCerrar, 0x42A042,
                "com/comercialvalerio/presentation/ui/icon/svg/check.svg");
        btnCerrar.addActionListener(e -> dispose());
        KeyUtils.setTooltipAndMnemonic(btnCerrar, KeyEvent.VK_C, "Cerrar");
        JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        btnPane.setOpaque(false);
        btnPane.add(btnCerrar);
        body.add(btnPane, "span 2, growx");
        getRootPane().setDefaultButton(btnCerrar);
    }

    public JLabel getLblId()        { return lblId; }
    public JLabel getLblFecha()     { return lblFecha; }
    public JLabel getLblEntrega()   { return lblEntrega; }
    public JLabel getLblDireccion() { return lblDireccion; }
    public JLabel getLblTipo()      { return lblTipo; }
    public JLabel getLblValeGas()   { return lblValeGas; }
    public JLabel getLblEmpleado()  { return lblEmpleado; }
    public JLabel getLblCliente()   { return lblCliente; }
    public JLabel getLblTotal()     { return lblTotal; }
    public JLabel getLblEstado()    { return lblEstado; }
    public JTextArea getLblComentario(){ return lblComentario; }
    public JTable getTblDetalles()  { return tblDetalles; }
    public JTable getTblPagos()     { return tblPagos; }
    public JScrollPane getSpDetalles() { return spDetalles; }
    public JScrollPane getSpPagos()    { return spPagos; }
    public JLabel getLblEmptyDetalles() { return lblEmptyDetalles; }
    public JLabel getLblEmptyPagos()    { return lblEmptyPagos; }

}

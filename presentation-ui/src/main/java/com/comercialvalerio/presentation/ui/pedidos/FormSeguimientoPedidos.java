package com.comercialvalerio.presentation.ui.pedidos;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.formdev.flatlaf.FlatClientProperties;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import com.comercialvalerio.presentation.controller.pedidos.SeguimientoPedidosController;
import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;
import com.comercialvalerio.presentation.ui.base.DatePickerField;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.DateFormatUtils;
import com.comercialvalerio.presentation.ui.common.BottomButtonsPanel;
import javax.swing.border.*;
import net.miginfocom.swing.MigLayout;

public class FormSeguimientoPedidos extends BaseForm implements Refreshable {

    /* ───── widgets públicos (para controlador) ───── */
    private final JComboBox<String> cboCategoria      = new JComboBox<>();
    private final JComboBox<String> cboProducto       = new JComboBox<>();
    private final JLabel            lblCategoria      = new JLabel("Categoría:");
    private final JLabel            lblProducto       = new JLabel("Producto:");
    private final DatePickerField   spDesde           = new DatePickerField();
    private final DatePickerField   spHasta           = new DatePickerField();
    private final JTable            tblPedidos        = new NonEditableTable();
    private final JScrollPane       spPedidos         = new JScrollPane(tblPedidos);
    private final JLabel            lblEmpty          = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JButton           btnVerDetalle     = new JButton("Ver Detalle");
    private final JButton           btnEditar         = new JButton("Editar Pedido");
    private final JButton           btnMarcarEntregado= new JButton("Marcar como entregado");
    private final JButton           btnCancelarPedido = new JButton("Cancelar Pedido");
    private final JButton           btnReimprimirOrden = new JButton("Imprimir Orden");
    private final JButton           btnDescargarOrden = new JButton("Descargar Orden");
    private final JButton           btnWhatsAppOrden  = new JButton("Enviar Orden por WhatsApp");
    private final JButton           btnReimprimirComp = new JButton("Imprimir Comprobante");
    private final JButton           btnDescargarComp  = new JButton("Descargar Comprobante");
    private final JButton           btnWhatsAppComp   = new JButton("Enviar Comprobante por WhatsApp");
    private final SeguimientoPedidosController controller;

    public FormSeguimientoPedidos() {
        buildUI();
        controller = new SeguimientoPedidosController(this);
        controller.cargarCategorias();
        cboCategoria.addActionListener(e -> controller.cargarProductos());
        cboProducto.addActionListener(e -> controller.refreshTable());
        spDesde.addDateSelectionListener(e -> controller.refreshTable());
        spHasta.addDateSelectionListener(e -> controller.refreshTable());
        btnVerDetalle.addActionListener(e -> controller.mostrarDetalle());
        btnEditar.addActionListener(e -> controller.editarPedido());
        btnMarcarEntregado.addActionListener(e -> controller.marcarEntregado());
        btnCancelarPedido.addActionListener(e -> controller.cancelarPedido());
        btnReimprimirOrden.addActionListener(e -> controller.reimprimirOrden());
        btnDescargarOrden.addActionListener(e -> controller.descargarOrden());
        btnWhatsAppOrden.addActionListener(e -> controller.enviarOrdenWhatsApp());
        btnReimprimirComp.addActionListener(e -> controller.reimprimirComprobante());
        btnDescargarComp.addActionListener(e -> controller.descargarComprobante());
        btnWhatsAppComp.addActionListener(e -> controller.enviarComprobanteWhatsApp());
        tblPedidos.getSelectionModel().addListSelectionListener(e -> updateButtons());
        tblPedidos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    controller.mostrarDetalle();
                }
            }
        });
        updateButtons();
    }

    private void buildUI() {
        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]", "[]16[grow]16[]push"));
        setOpaque(false);
        putClientProperty(FlatClientProperties.STYLE, "background:@background");

        /* ----------- Encabezado ----------- */
        JButton btnRefresh = UIUtils.createRefreshButton(this::refresh);
        HeaderPanel header = new HeaderPanel("Seguimiento de Pedidos", btnRefresh);
        header.setBorder(new EmptyBorder(0,0,5,0));

        add(header, "cell 0 0, growx, wrap");

        /* ----------- Cuerpo ----------- */
        JPanel body = new JPanel(new MigLayout(
            "insets 0, gap 10, fill",
            "[grow,fill]",
            "[]16[grow]16[]push"));
        body.setOpaque(false);
        add(body, "cell 0 1, grow");

        // Filtros
        JPanel filtros = new JPanel(new MigLayout("insets 0, gap " + UIStyle.FILTER_GAP));
        filtros.setOpaque(false);
        spDesde.setDateFormat(DateFormatUtils.getShortPattern());
        spHasta.setDateFormat(DateFormatUtils.getShortPattern());
        cboCategoria.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXX");
        cboProducto .setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXX");
        lblCategoria.setLabelFor(cboCategoria);
        lblProducto.setLabelFor(cboProducto);
        filtros.add(new JLabel("Desde:"));
        filtros.add(spDesde, UIStyle.DATE_FIELD_CONSTRAINTS);
        filtros.add(new JLabel("Hasta:"));
        filtros.add(spHasta, UIStyle.DATE_FIELD_CONSTRAINTS);
        filtros.add(lblCategoria);
        filtros.add(cboCategoria, "w 230!, h " + UIStyle.COMBO_HEIGHT + "!");
        filtros.add(lblProducto);
        filtros.add(cboProducto,  "w 230!, h " + UIStyle.COMBO_HEIGHT + "!");
        body.add(filtros, "cell 0 0, growx, wrap");

        // Tabla de pedidos
        tblPedidos.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        body.add(spPedidos, "cell 0 1, grow");

        // Botones abajo
        // Ver Detalle
        ButtonStyles.styleBottom(btnVerDetalle, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/eye.svg");

        KeyUtils.setTooltipAndMnemonic(btnVerDetalle, KeyEvent.VK_V, "Ver Detalle");
        // Editar
        ButtonStyles.styleBottom(btnEditar, 0xFFA500,
                "com/comercialvalerio/presentation/ui/icon/svg/edit.svg");

        KeyUtils.setTooltipAndMnemonic(btnEditar, KeyEvent.VK_E, "Editar Pedido");
        // Marcar como entregado
        ButtonStyles.styleBottom(btnMarcarEntregado, 0x42A042,
                "com/comercialvalerio/presentation/ui/icon/svg/check.svg");

        KeyUtils.setTooltipAndMnemonic(btnMarcarEntregado, KeyEvent.VK_M, "Marcar como entregado");
        // Cancelar Pedido
        ButtonStyles.styleBottom(btnCancelarPedido, 0xE74C3C,
                "com/comercialvalerio/presentation/ui/icon/svg/close_circle.svg");
        KeyUtils.setTooltipAndMnemonic(btnCancelarPedido, KeyEvent.VK_C, "Cancelar Pedido");
        // Imprimir Orden
        ButtonStyles.styleBottom(btnReimprimirOrden, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/printer.svg");
        KeyUtils.setTooltipAndMnemonic(btnReimprimirOrden, KeyEvent.VK_R, "Imprimir Orden");
        // Descargar Orden
        ButtonStyles.styleBottom(btnDescargarOrden, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        KeyUtils.setTooltipAndMnemonic(btnDescargarOrden, KeyEvent.VK_O, "Descargar Orden");
        // WhatsApp Orden
        ButtonStyles.styleBottom(btnWhatsAppOrden, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/whatsapp.svg");
        KeyUtils.setTooltipAndMnemonic(btnWhatsAppOrden, KeyEvent.VK_P, "Enviar Orden por WhatsApp");
        // Imprimir Comprobante
        ButtonStyles.styleBottom(btnReimprimirComp, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/printer.svg");
        KeyUtils.setTooltipAndMnemonic(btnReimprimirComp, KeyEvent.VK_I, "Imprimir Comprobante");
        // Descargar Comprobante
        ButtonStyles.styleBottom(btnDescargarComp, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        KeyUtils.setTooltipAndMnemonic(btnDescargarComp, KeyEvent.VK_D, "Descargar Comprobante");
        // WhatsApp Comprobante
        ButtonStyles.styleBottom(btnWhatsAppComp, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/whatsapp.svg");
        KeyUtils.setTooltipAndMnemonic(btnWhatsAppComp, KeyEvent.VK_W, "Enviar Comprobante por WhatsApp");

        JPanel panelOrden = new BottomButtonsPanel(
                btnVerDetalle, btnEditar, btnMarcarEntregado, btnCancelarPedido,
                btnReimprimirOrden, btnDescargarOrden, btnWhatsAppOrden);
        body.add(panelOrden, "cell 0 2, alignx center, wrap");

        JPanel panelComp = new BottomButtonsPanel(
                btnReimprimirComp, btnDescargarComp, btnWhatsAppComp);
        body.add(panelComp, "cell 0 3, alignx center");

        /* ----------- Borde exterior ----------- */
        setBorder(UIStyle.FORM_BORDER_VIOLET);
    }

    /* ===== getters para controlador ===== */
    public JComboBox<String> getCboCategoria()       { return cboCategoria;       }
    public JComboBox<String> getCboProducto()        { return cboProducto;        }
    public JTable            getTblPedidos()         { return tblPedidos;         }
    public JScrollPane       getSpPedidos()          { return spPedidos;          }
    public JLabel            getLblEmpty()           { return lblEmpty;           }
    public DatePickerField   getSpDesde()            { return spDesde;            }
    public DatePickerField   getSpHasta()            { return spHasta;            }
    public JButton           getBtnVerDetalle()      { return btnVerDetalle; }
    public JButton           getBtnEditar()          { return btnEditar;          }
    public JButton           getBtnMarcarEntregado() { return btnMarcarEntregado; }
    public JButton           getBtnCancelarPedido()  { return btnCancelarPedido;  }
    public JButton           getBtnReimprimirOrden() { return btnReimprimirOrden; }
    public JButton           getBtnDescargarOrden()  { return btnDescargarOrden; }
    public JButton           getBtnWhatsAppOrden()   { return btnWhatsAppOrden; }
    public JButton           getBtnReimprimirComp()  { return btnReimprimirComp; }
    public JButton           getBtnDescargarComp()   { return btnDescargarComp; }
    public JButton           getBtnWhatsAppComp()    { return btnWhatsAppComp;  }

    /** Habilita o deshabilita botones según la fila seleccionada. */
    public void updateButtons() {
        int row = tblPedidos.getSelectedRow();
        boolean sel = row >= 0;
        boolean editar = false;
        boolean cancelar = false;
        btnReimprimirOrden.setEnabled(sel);
        btnDescargarOrden.setEnabled(sel);
        btnWhatsAppOrden.setEnabled(sel);
        boolean entregar = false;
        boolean reimp = false;
        if (sel) {
            int modelRow = tblPedidos.convertRowIndexToModel(row);
            Object est = tblPedidos.getModel().getValueAt(modelRow, 6);
            if (est != null) {
                String s = est.toString();
                entregar = "En Proceso".equalsIgnoreCase(s);
                cancelar = "En Proceso".equalsIgnoreCase(s);
                editar = !"Entregada".equalsIgnoreCase(s)
                        && !"Cancelada".equalsIgnoreCase(s);
                reimp = "Completada".equalsIgnoreCase(s)
                        || "Entregada".equalsIgnoreCase(s);
            }
        }
        btnEditar.setEnabled(sel && editar);
        btnVerDetalle.setEnabled(sel);
        btnCancelarPedido.setEnabled(cancelar);
        btnMarcarEntregado.setEnabled(entregar);
        btnReimprimirComp.setEnabled(reimp);
        btnDescargarComp.setEnabled(reimp);
        btnWhatsAppComp.setEnabled(reimp);
    }

    @Override
    public void refresh() {
        controller.refreshTable();
    }

    @Override
    protected void registerShortcuts() {
        KeyUtils.registerRefreshAction(this, controller::refreshTable);
        KeyUtils.registerFocusAction(this, spDesde);
    }
}

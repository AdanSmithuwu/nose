package com.comercialvalerio.presentation.ui.ventas;

import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.comercialvalerio.presentation.controller.ventas.SeguimientoVentasController;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.base.DatePickerField;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import com.comercialvalerio.presentation.ui.common.BottomButtonsPanel;
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.DateFormatUtils;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

/**
 * Seguimiento de ventas: tabla filtrable por categoría o producto.
 */
public class FormSeguimientoVentas extends BaseForm implements Refreshable {

    /* ───── widgets públicos para el controlador ───── */
    private final JComboBox<String> cboCategoria = new JComboBox<>();
    private final JComboBox<String> cboProducto  = new JComboBox<>();
    private final JLabel lblCategoria = new JLabel("Categoría:");
    private final JLabel lblProducto  = new JLabel("Producto:");
    private final DatePickerField   spDesde     = new DatePickerField();
    private final DatePickerField   spHasta     = new DatePickerField();
    private final JTable             tblVentas   = new NonEditableTable();
    private final JScrollPane        spVentas   = new JScrollPane(tblVentas);
    private final JLabel             lblEmpty    = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JButton            btnRefresh;
    private final JButton            btnVerDetalle = new JButton("Ver Detalle");
    private final JButton            btnReimprimir = new JButton("Imprimir comprobante");
    private final JButton            btnDescargar  = new JButton("Descargar Comprobante");
    private final JButton            btnWhatsApp   = new JButton("Enviar Comprobante por Whatsapp");
    private final JButton            btnCancelar = new JButton("Cancelar Venta");
    private final SeguimientoVentasController controller;

    public FormSeguimientoVentas() {
        controller = new SeguimientoVentasController(this);
        btnRefresh = UIUtils.createRefreshButton(controller::refreshTable);
        buildUI();
        controller.cargarCategorias();
        cboCategoria.addActionListener(e -> controller.cargarProductos());
        cboProducto.addActionListener(e -> controller.refreshTable());
        spDesde.addDateSelectionListener(e -> controller.refreshTable());
        spHasta.addDateSelectionListener(e -> controller.refreshTable());
        btnCancelar.addActionListener(e -> controller.cancelarVenta());
        btnReimprimir.addActionListener(e -> controller.reimprimirComprobante());
        btnDescargar.addActionListener(e -> controller.descargarComprobante());
        btnWhatsApp.addActionListener(e -> controller.enviarComprobanteWhatsApp());
        btnVerDetalle.addActionListener(e -> controller.mostrarDetalle());
        tblVentas.getSelectionModel().addListSelectionListener(e -> updateButtons());
        tblVentas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    controller.mostrarDetalle();
                }
            }
        });
        updateButtons();
    }

    /* ====================================================================== */
    /*  C O N S T R U C C I Ó N   D E   L A   I N T E R F A Z                 */
    /* ====================================================================== */
    private void buildUI() {

        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]", "[]16[grow]16[]push"));
        setOpaque(false);
        putClientProperty(FlatClientProperties.STYLE, "background:@background");

        /* ---------------- encabezado --------------------------------- */
        HeaderPanel header = new HeaderPanel("Seguimiento de Ventas", btnRefresh);
        header.setBorder(new EmptyBorder(0,0,5,0));
        add(header, "cell 0 0, growx, wrap");

        /* ---------------- cuerpo --------------------------------------- */
        JPanel body = new JPanel(new MigLayout(
                "insets 0, gap 10, fill",             // separación global de 10px
                "[grow,fill]",
                "[]16[grow]16[]push"));               // 16px entre filas
        body.setOpaque(false);
        add(body, "cell 0 1, grow");

        /* ---- Filtros (fechas/categoría/producto) -------------------- */
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
        filtros.add(cboProducto , "w 230!, h " + UIStyle.COMBO_HEIGHT + "!");
        body.add(filtros, "cell 0 0, growx, wrap");

        /* ---- Tabla ---------------------------------------------------- */
        tblVentas.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        body.add(spVentas, "cell 0 1, grow");

        /* ---- Botón Ver Detalle --------------------------------------- */
        ButtonStyles.styleBottom(btnVerDetalle, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/eye.svg");
        KeyUtils.setTooltipAndMnemonic(btnVerDetalle, KeyEvent.VK_V, "Ver Detalle");

        /* ---- Botón Cancelar Venta ------------------------------------ */
        ButtonStyles.styleBottom(btnCancelar, 0xE74C3C,
                "com/comercialvalerio/presentation/ui/icon/svg/close_circle.svg");
        KeyUtils.setTooltipAndMnemonic(btnCancelar, KeyEvent.VK_C, "Cancelar Venta");

        /* ---- Botón Imprimir Comprobante ------------------------ */
        ButtonStyles.styleBottom(btnReimprimir, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/printer.svg");
        KeyUtils.setTooltipAndMnemonic(btnReimprimir, KeyEvent.VK_R, "Imprimir Comprobante");

        /* ---- Botón Descargar ---------------------------------------- */
        ButtonStyles.styleBottom(btnDescargar, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        KeyUtils.setTooltipAndMnemonic(btnDescargar, KeyEvent.VK_D, "Descargar Comprobante");

        /* ---- Botón WhatsApp ------------------------------------------- */
        ButtonStyles.styleBottom(btnWhatsApp, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/whatsapp.svg");
        KeyUtils.setTooltipAndMnemonic(btnWhatsApp, KeyEvent.VK_W, "Enviar Comprobante por Whatsapp");

        JPanel south = new BottomButtonsPanel(
                btnVerDetalle, btnCancelar, btnReimprimir, btnDescargar, btnWhatsApp);
        body.add(south, "cell 0 2, alignx center");

        /* ---- borde violeta ------------------------------------------- */
        setBorder(UIStyle.FORM_BORDER_VIOLET);
    }

    /* ============ getters para conectar lógica externa ================== */
    public JComboBox<String> getCboCategoria() { return cboCategoria; }
    public JComboBox<String> getCboProducto()  { return cboProducto;  }
    public JTable            getTblVentas()    { return tblVentas;    }
    public JScrollPane       getSpVentas()     { return spVentas;     }
    public JLabel            getLblEmpty()     { return lblEmpty;     }
    public DatePickerField   getSpDesde()     { return spDesde;     }
    public DatePickerField   getSpHasta()     { return spHasta;     }
    public JButton           getBtnRefresh()   { return btnRefresh;   }
    public JButton           getBtnReimprimir(){ return btnReimprimir; }
    public JButton           getBtnDescargar() { return btnDescargar;  }
    public JButton           getBtnWhatsApp()  { return btnWhatsApp;   }
    public JButton           getBtnCancelar()  { return btnCancelar;  }
    public JButton           getBtnVerDetalle(){ return btnVerDetalle; }

    /** Habilita o deshabilita botones según la selección en la tabla. */
    public final void updateButtons() {
        int row = tblVentas.getSelectedRow();
        boolean sel = row >= 0;
        boolean cancelar = false;
        boolean reimp = false;
        if (sel) {
            int modelRow = tblVentas.convertRowIndexToModel(row);
            Object est = tblVentas.getModel().getValueAt(modelRow, 5);
            if (est != null) {
                String s = est.toString();
                cancelar = !"Cancelada".equalsIgnoreCase(s);
                reimp = "Completada".equalsIgnoreCase(s)
                        || "Entregada".equalsIgnoreCase(s);
            }
        }
        btnCancelar.setEnabled(sel && cancelar);
        btnReimprimir.setEnabled(reimp);
        btnDescargar.setEnabled(reimp);
        btnWhatsApp.setEnabled(reimp);
        btnVerDetalle.setEnabled(sel);
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

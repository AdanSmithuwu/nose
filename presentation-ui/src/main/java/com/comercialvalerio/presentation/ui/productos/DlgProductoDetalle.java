package com.comercialvalerio.presentation.ui.productos;

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

/** Diálogo que muestra información del producto con sus tallas o presentaciones. */
public class DlgProductoDetalle extends BaseDialog {

    private final JLabel lblNombre   = new JLabel();
    private final JLabel lblCategoria= new JLabel();
    private final JLabel lblTipo     = new JLabel();
    private final JLabel lblPrecio   = new JLabel();
    private final JLabel lblPrecioTxt= new JLabel("Precio:");
    private final JLabel lblTable    = new JLabel();
    private final JTable tblDatos    = new NonEditableTable();
    private final JScrollPane spDatos= new JScrollPane(tblDatos);
    private final JLabel lblEmpty    = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JButton btnCerrar;

    public DlgProductoDetalle(Window owner) {
        super(owner, "Detalle de Producto", ModalityType.APPLICATION_MODAL, new JButton("Cerrar"));
        this.btnCerrar = getDefaultButton();
        buildUI();
        SwingUtilities.invokeLater(() -> btnCerrar.requestFocusInWindow());
        pack();
        Dimension size = getSize();
        size.height += 20; // aumenta la altura para que el botón no se corte
        setSize(size);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.putClientProperty(FlatClientProperties.STYLE, "background:@background");
        root.setBackground(UIStyle.getColorCardBg());
        // Margen inferior para evitar que el botón se oculte parcialmente
        root.setBorder(new EmptyBorder(20,20,20,20));
        setContentPane(root);

        // El título ya está en la barra de la ventana

        JPanel body = new JPanel(new MigLayout("fillx, wrap 2, insets 0, gap 8","[right][grow,fill]",""));
        body.setOpaque(false);
        root.add(body, BorderLayout.CENTER);

        body.add(new JLabel("Nombre:"));    body.add(lblNombre);
        body.add(new JLabel("Categoría:")); body.add(lblCategoria);
        body.add(new JLabel("Tipo:"));      body.add(lblTipo);
        body.add(lblPrecioTxt);             body.add(lblPrecio);
        lblPrecioTxt.setVisible(false);
        lblPrecio.setVisible(false);

        body.add(lblTable, "span 2, gaptop 10");
        tblDatos.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        body.add(spDatos, "span 2, grow, h 120!");

        ButtonStyles.styleBottom(btnCerrar, 0x42A042,
                "com/comercialvalerio/presentation/ui/icon/svg/check.svg");
        KeyUtils.setTooltipAndMnemonic(btnCerrar, KeyEvent.VK_C, "Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        btnPane.setOpaque(false);
        btnPane.add(btnCerrar);
        body.add(btnPane, "span 2, growx");
        getRootPane().setDefaultButton(btnCerrar);
    }

    public JLabel getLblNombre()    { return lblNombre;    }
    public JLabel getLblCategoria() { return lblCategoria; }
    public JLabel getLblTipo()      { return lblTipo;      }
    public JLabel getLblPrecio()    { return lblPrecio;    }
    public JLabel getLblPrecioTxt() { return lblPrecioTxt; }
    public JLabel getLblTable()     { return lblTable;     }
    public JTable getTblDatos()     { return tblDatos;     }
    public JScrollPane getSpDatos() { return spDatos;     }
    public JLabel getLblEmpty()     { return lblEmpty;     }
}

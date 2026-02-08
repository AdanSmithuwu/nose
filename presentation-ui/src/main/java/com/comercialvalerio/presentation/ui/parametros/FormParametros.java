package com.comercialvalerio.presentation.ui.parametros;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.formdev.flatlaf.FlatClientProperties;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import java.beans.PropertyChangeListener;
import javax.swing.UIManager;
import com.comercialvalerio.presentation.controller.parametros.ParametroSistemaController;

import javax.swing.*;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.common.BottomButtonsPanel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import net.miginfocom.swing.MigLayout;
import java.awt.event.KeyEvent;

/** Panel que lista los parámetros del sistema editables. */
public class FormParametros extends BaseForm implements Refreshable {

    private final JButton btnRefresh;
    private final JButton btnEditar  = new JButton("Editar");
    private final JTable tabla = new NonEditableTable();
    private final JScrollPane sp = new JScrollPane(tabla);
    private final JLabel lblEmpty = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final ParametroSistemaController controller = new ParametroSistemaController(this);
    private final JLabel lblTitulo = new JLabel("Parámetros del Sistema");
    private final PropertyChangeListener lafListener = e -> {
        if ("lookAndFeel".equals(e.getPropertyName())) {
            updateTitleStyle();
        }
    };

    public FormParametros() {
        btnRefresh = UIUtils.createRefreshButton(controller::refresh);
        buildUI();
        KeyUtils.setTooltipAndMnemonic(btnEditar, KeyEvent.VK_E, "Editar");
        btnEditar.addActionListener(e -> controller.editarSeleccionado());
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    controller.editarSeleccionado();
                }
            }
        });
        tabla.getSelectionModel().addListSelectionListener(e -> updateButtons());
        controller.refresh();
        updateButtons();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        UIManager.addPropertyChangeListener(lafListener);
    }

    @Override
    public void removeNotify() {
        UIManager.removePropertyChangeListener(lafListener);
        super.removeNotify();
    }

    private void buildUI() {
        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow][grow][pref!]",
                "[][pref!][grow][pref!]"));
        setOpaque(false);
        putClientProperty(FlatClientProperties.STYLE, "background:@background");

        updateTitleStyle();
        add(lblTitulo, "cell 0 0, growx");

        add(btnRefresh, "cell 2 0, right, wrap");

        tabla.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        add(sp, "cell 0 2 3 1, grow");

        ButtonStyles.styleBottom(btnEditar, 0xF47B20,
                "com/comercialvalerio/presentation/ui/icon/svg/edit.svg");
        JPanel south = new BottomButtonsPanel(btnEditar);
        add(south, "cell 0 3 3 1, alignx center");

        setBorder(UIStyle.FORM_BORDER_VIOLET);
    }

    public JTable getTabla() { return tabla; }
    public JScrollPane getScroll() { return sp; }
    public JLabel getLblEmpty() { return lblEmpty; }
    public JButton getBtnEditar() { return btnEditar; }

    /** Habilita el botón Editar si hay una fila seleccionada. */
    public void updateButtons() {
        boolean sel = tabla.getSelectedRow() >= 0;
        btnEditar.setEnabled(sel);
    }

    @Override
    public void refresh() {
        controller.refresh();
    }

    @Override
    protected void registerShortcuts() {
        KeyUtils.registerRefreshAction(this, controller::refresh);
    }

    private void updateTitleStyle() {
        lblTitulo.putClientProperty(FlatClientProperties.STYLE,
                "font:$h1.font; foreground:" + UIStyle.getHexDarkText());
    }
}

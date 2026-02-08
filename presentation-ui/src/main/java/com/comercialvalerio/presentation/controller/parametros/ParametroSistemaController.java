package com.comercialvalerio.presentation.controller.parametros;

import com.comercialvalerio.application.dto.ParametroSistemaCreateDto;
import com.comercialvalerio.application.dto.ParametroSistemaDto;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.ui.parametros.DlgParametroEditar;
import com.comercialvalerio.presentation.ui.parametros.FormParametros;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.util.DigitFilter;
import com.comercialvalerio.presentation.ui.util.NumericFilter;

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Locale;

/** Controlador para {@link FormParametros}. */
public class ParametroSistemaController {

    private final FormParametros view;
    private List<ParametroSistemaDto> listaActual = List.of();
    private static final Logger LOG = Logger.getLogger(ParametroSistemaController.class.getName());

    public ParametroSistemaController(FormParametros view) {
        this.view = view;
    }

    /** Carga los parámetros del servicio y llena la tabla. */
    public void refresh() {
        // Cargar parámetros de forma asíncrona
        AsyncTasks.busy(view, () -> {
            listaActual = UiContext.parametroSistemaSvc().listar();
            String[] cols = {"Clave", "Valor", "Descripción"};
            DefaultTableModel m = new DefaultTableModel(cols, 0);
            for (ParametroSistemaDto p : listaActual) {
                String val = isIntegerParam(p.clave())
                        ? p.valor().stripTrailingZeros().toPlainString()
                        : p.valor().toPlainString();
                m.addRow(new Object[]{p.clave(), val, p.descripcion()});
            }
            return m;
        }, m -> {
            view.getTabla().setModel(m);
            TableUtils.packColumns(view.getTabla());
            TableUtils.updateEmptyView(
                    view.getScroll(),
                    view.getTabla(),
                    view.getLblEmpty());
            view.updateButtons();
        });
    }

    /** Abre un diálogo de edición para el parámetro seleccionado y guarda los cambios. */
    public void editarSeleccionado() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0) return;
        ParametroSistemaDto sel = listaActual.get(row);
        try {
            AsyncTasks.busy(view, () -> UiContext.parametroSistemaSvc().obtener(sel.clave()), dto -> {
                JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(view);
                if ("ID_CLIENTE_GENERICO".equalsIgnoreCase(dto.clave())) {
                    JOptionPane.showMessageDialog(owner,
                            "Modificar este valor podr\u00eda causar errores graves en el sistema, por favor, proceda con precauci\u00f3n",
                            "Advertencia", JOptionPane.WARNING_MESSAGE);
                }
                DlgParametroEditar dlg = new DlgParametroEditar(owner);
                dlg.getTxtClave().setText(dto.clave());
                dlg.getTxtDescripcion().setText(dto.descripcion());
                if (isIntegerParam(dto.clave())) {
                    ((AbstractDocument) dlg.getTxtValor().getDocument())
                            .setDocumentFilter(new DigitFilter(dlg.getTxtValor()));
                    dlg.getTxtValor().setText(dto.valor().stripTrailingZeros().toPlainString());
                } else {
                    ((AbstractDocument) dlg.getTxtValor().getDocument())
                            .setDocumentFilter(new NumericFilter(dlg.getTxtValor()));
                    dlg.getTxtValor().setText(dto.valor().toPlainString());
                }

            for (var al : dlg.getBtnGuardar().getActionListeners()) {
                dlg.getBtnGuardar().removeActionListener(al);
            }

            dlg.getBtnGuardar().addActionListener(ev -> {
                String valor = dlg.getTxtValor().getText().trim();
                if (!validarValor(dto.clave(), valor, dlg)) {
                    return;
                }
                AsyncTasks.busy(view, () -> {
                    Integer idEmp = UiContext.getUsuarioActual().idPersona();
                    java.math.BigDecimal val = new java.math.BigDecimal(valor);
                    ParametroSistemaCreateDto chg = new ParametroSistemaCreateDto(
                            dto.clave(), val, dto.descripcion(), idEmp);
                    UiContext.parametroSistemaSvc().guardar(dto.clave(), chg);
                    return null;
                }, v -> {
                    dlg.dispose();
                    refresh();
                });
            });

            dlg.setVisible(true);
        });
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al editar parámetro", ex);
            ErrorHandler.handle(ex);
        }
    }

    private boolean validarValor(String clave, String valor, JDialog dlg) {
        String upper = clave.toUpperCase(Locale.ROOT);
        if (isIntegerParam(upper)) {
            if (!valor.matches("\\d+")) {
                JOptionPane.showMessageDialog(dlg,
                        "Valor para " + clave + " debe ser un n\u00famero entero",
                        "Dato inv\u00e1lido", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            int v = Integer.parseInt(valor);
            if (v < 0) {
                JOptionPane.showMessageDialog(dlg,
                        "Valor para " + clave + " debe ser mayor o igual a 0",
                        "Dato inv\u00e1lido", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        }
        if (!valor.matches("\\d+(\\.\\d+)?")) {
            JOptionPane.showMessageDialog(dlg,
                    "Valor para " + clave + " debe ser num\u00e9rico",
                    "Dato inv\u00e1lido", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        java.math.BigDecimal bd = new java.math.BigDecimal(valor);
        if (bd.compareTo(java.math.BigDecimal.ZERO) < 0) {
            JOptionPane.showMessageDialog(dlg,
                    "Valor para " + clave + " debe ser mayor o igual a 0",
                    "Dato inv\u00e1lido", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean isIntegerParam(String clave) {
        String u = clave.toUpperCase(Locale.ROOT);
        return u.contains("CANTIDAD") || u.contains("MARGEN")
                || u.equals("MAX_INTENTOS_FALLIDOS")
                || u.equals("MINUTOS_BLOQUEO_CUENTA")
                || u.equals("ID_CLIENTE_GENERICO");
    }
}

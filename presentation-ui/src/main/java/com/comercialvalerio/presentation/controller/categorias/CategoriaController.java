package com.comercialvalerio.presentation.controller.categorias;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comercialvalerio.presentation.ui.util.DialogUtils;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.util.Locale;

import com.comercialvalerio.application.dto.CategoriaCreateDto;
import com.comercialvalerio.application.dto.CategoriaDto;
import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.ui.categorias.DlgCategoriaEditar;
import com.comercialvalerio.presentation.ui.categorias.DlgCategoriaNueva;
import com.comercialvalerio.presentation.ui.categorias.FormCategorias;
import com.comercialvalerio.application.dto.EstadoNombre;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.application.dto.RolNombre;

/** Controlador para {@link FormCategorias}. */
public class CategoriaController {

    private final FormCategorias view;
    private static final Logger LOG = Logger.getLogger(CategoriaController.class.getName());
    private java.util.List<CategoriaDto> listaActual = java.util.List.of();
    private java.util.List<String> dependencias = java.util.List.of();

    public CategoriaController(FormCategorias view) {
        this.view = view;
    }

    public void cargarDependencias() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0 || row >= listaActual.size()) {
            dependencias = java.util.List.of();
            return;
        }
        CategoriaDto sel = listaActual.get(row);
        try {
            dependencias = UiContext.categoriaSvc().obtenerDependencias(sel.idCategoria());
        } catch (RuntimeException ex) {
            ErrorHandler.handle(ex);
            dependencias = java.util.List.of();
        }
    }

    public java.util.List<String> getDependencias() { return dependencias; }

    /** Recarga la lista de categorías en la tabla. */
    public void refresh() {
        AsyncTasks.busy(view, () -> {
            listaActual = UiContext.categoriaSvc().listar();
            return null;
        }, v -> {
            String[] cols = {"Nombre", "Descripción", "Estado"};
            DefaultTableModel m = new DefaultTableModel(cols, 0);
            for (CategoriaDto c : listaActual) {
                m.addRow(new Object[]{c.nombre(), c.descripcion(), c.estado()});
            }
            view.getTabla().setModel(m);
            TableUtils.packColumns(view.getTabla());
            TableUtils.updateEmptyView(
                    view.getScroll(),
                    view.getTabla(),
                    view.getLblEmpty());
            view.updateButtons();
        });
    }

    /** Filtra la tabla según el texto del cuadro de búsqueda. */
    public void buscar() {
        String filtro = view.getTxtBuscar().getText().trim().toLowerCase(Locale.ROOT);
        java.util.List<CategoriaDto> lista = listaActual;
        if (!filtro.isBlank()) {
            String f = filtro;
            lista = listaActual.stream()
                    .filter(c -> c.nombre() != null && c.nombre().toLowerCase(Locale.ROOT).contains(f))
                    .toList();
        }
        String[] cols = {"Nombre", "Descripción", "Estado"};
        DefaultTableModel m = new DefaultTableModel(cols, 0);
        for (CategoriaDto c : lista) {
            m.addRow(new Object[]{c.nombre(), c.descripcion(), c.estado()});
        }
        view.getTabla().setModel(m);
        TableUtils.packColumns(view.getTabla());
        TableUtils.updateEmptyView(
                view.getScroll(),
                view.getTabla(),
                view.getLblEmpty());
        view.updateButtons();
    }

    /** Abre el diálogo de nueva categoría y luego refresca la lista. */
    public void nueva() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(view);
        new DlgCategoriaNueva(owner).setVisible(true);
        refresh();
    }

    /** Abre un diálogo de edición para la fila seleccionada. */
    public void editarSeleccionado() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0 || row >= listaActual.size()) {
            return;
        }
        CategoriaDto sel = listaActual.get(row);
        AsyncTasks.busy(view, () -> UiContext.categoriaSvc().obtener(sel.idCategoria()), dto -> {
            JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(view);
            DlgCategoriaEditar dlg = new DlgCategoriaEditar(owner);
            dlg.getTxtNombre().setText(dto.nombre());
            dlg.getTxtDescripcion().setText(dto.descripcion());

            for (var al : dlg.getBtnGuardar().getActionListeners()) {
                dlg.getBtnGuardar().removeActionListener(al);
            }

            dlg.getBtnGuardar().addActionListener(ev -> {
                String nom = dlg.getTxtNombre().getText().trim();
                String desc = dlg.getTxtDescripcion().getText().trim();
                if (nom.isBlank()) {
                    JOptionPane.showMessageDialog(dlg,
                            "Ingrese el nombre de la categoría",
                            "Datos incompletos", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                AsyncTasks.busy(view, () -> {
                    CategoriaCreateDto chg = new CategoriaCreateDto(nom,
                            desc.isEmpty() ? null : desc);
                    UiContext.categoriaSvc().actualizar(dto.idCategoria(), chg);
                    return null;
                }, v -> {
                    dlg.dispose();
                    refresh();
                    raven.toast.Notifications.getInstance()
                            .show(raven.toast.Notifications.Type.SUCCESS,
                                    "Categoría actualizada");
                });
            });

            dlg.setVisible(true);
        });
    }

    /** Elimina la categoría seleccionada tras la confirmación. */
    public void eliminarSeleccionado() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0 || row >= listaActual.size()) return;
        if (!isAdmin()) {
            JOptionPane.showMessageDialog(view,
                    "Sólo un administrador puede eliminar categorías",
                    "Permiso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CategoriaDto sel = listaActual.get(row);
        boolean ok = DialogUtils.confirmAction(view,
                """
                ¿Eliminar permanentemente la categoría seleccionada?
                Esta acción es irreversible
                """.stripTrailing());
        if (!ok) return;
        if (!dependencias.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    "No se puede eliminar, dependencias:\n- " + String.join("\n- ", dependencias),
                    "Dependencias", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AsyncTasks.busy(view, () -> {
            UiContext.categoriaSvc().eliminar(sel.idCategoria());
            return null;
        }, v -> {
            refresh();
        raven.toast.Notifications.getInstance()
                .show(raven.toast.Notifications.Type.SUCCESS,
                        "Categoría eliminada");
        });
    }

    /** Activa la categoría seleccionada. */
    public void activarSeleccionado() {
        cambiarEstadoSeleccionado(EstadoNombre.ACTIVO.getNombre());
    }

    /** Desactiva la categoría seleccionada. */
    public void desactivarSeleccionado() {
        cambiarEstadoSeleccionado(EstadoNombre.INACTIVO.getNombre());
    }

    /** Cambia o establece el estado de la categoría seleccionada. */
    public void cambiarEstadoSeleccionado() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0 || row >= listaActual.size()) return;
        CategoriaDto sel = listaActual.get(row);
        String nuevo = EstadoNombre.ACTIVO.getNombre().equalsIgnoreCase(sel.estado())
                ? EstadoNombre.INACTIVO.getNombre() : EstadoNombre.ACTIVO.getNombre();
        cambiarEstado(sel.idCategoria(), nuevo);
    }

    private void cambiarEstadoSeleccionado(String nuevo) {
        int row = view.getTabla().getSelectedRow();
        if (row < 0 || row >= listaActual.size()) return;
        CategoriaDto sel = listaActual.get(row);
        cambiarEstado(sel.idCategoria(), nuevo);
    }

    private void cambiarEstado(Integer id, String nuevo) {
        int afectados;
        try {
            afectados = UiContext.productoSvc()
                    .listar(null, id, null, null, null)
                    .size();
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error listing products", ex);
            ErrorHandler.handle(ex);
            afectados = 0;
        }
        String msg = (EstadoNombre.ACTIVO.getNombre().equalsIgnoreCase(nuevo) ? "Reactivar" : "Desactivar") +
                " la categoría seleccionada?\n" +
                (afectados > 0 ? afectados + " productos cambiarán de estado" : "Sin productos afectados");
        if (!DialogUtils.confirmAction(view, msg)) return;
        AsyncTasks.busy(view, () -> {
            UiContext.categoriaSvc().cambiarEstado(id, new CambiarEstadoDto(nuevo), true);
            return null;
        }, v -> refresh());
    }

    /** Devuelve {@code true} si el usuario actual tiene rol de administrador. */
    public boolean isAdmin() {
        return UiContext.getUsuarioActual() != null
                && RolNombre.fromNombre(UiContext.getUsuarioActual().rolNombre()) == RolNombre.ADMINISTRADOR;
    }
}

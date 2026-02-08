package com.comercialvalerio.presentation.controller.clientes;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.util.Locale;

import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.dto.ClienteCreateDto;
import com.comercialvalerio.application.dto.ClienteDto;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.clientes.DlgClienteEditar;
import com.comercialvalerio.presentation.ui.clientes.FormClientes;
import com.comercialvalerio.application.dto.EstadoNombre;
import com.comercialvalerio.application.dto.RolNombre;
import com.comercialvalerio.common.PhoneUtils;
import com.comercialvalerio.presentation.ui.historial.DlgHistorialCliente;
import com.comercialvalerio.presentation.ui.util.DialogUtils;

/**
 * Controlador para {@link FormClientes}.
 * Carga datos de clientes usando {@link com.comercialvalerio.application.service.ClienteService} y permite filtrar
 * por nombre o teléfono.
 */
public class ClienteController {

    private final FormClientes view;
    private static final String GENERIC_DNI = "00000000";
    private static final Logger LOG = Logger.getLogger(ClienteController.class.getName());
    private java.util.List<String> dependencias = java.util.List.of();

    public ClienteController(FormClientes view) {
        this.view = view;
        this.view.getTabla().getSelectionModel().addListSelectionListener(e -> cargarDependencias());
    }

    /** Actualiza el contenido de la tabla aplicando el filtro de búsqueda actual. */
    public void refresh() {
        String filtro = view.getTxtBuscar().getText().trim().toLowerCase(Locale.ROOT);
        String estado = ((String) view.getCboEstado().getSelectedItem());
        AsyncTasks.busy(view, () -> {
            java.util.List<ClienteDto> base =
                    "Todos".equalsIgnoreCase(estado)
                            ? UiContext.clienteSvc().listar()
                            : UiContext.clienteSvc().findByEstado(estado);
            return base.stream()
                    .filter(c -> !GENERIC_DNI.equals(c.dni()))
                    .filter(c -> {
                        if (filtro.isBlank()) return true;
                        String nombre = c.nombreCompleto().toLowerCase(Locale.ROOT);
                        if (filtro.matches("\\d+")) {
                            boolean tel = c.telefono() != null && c.telefono().contains(filtro);
                            return tel || nombre.contains(filtro);
                        }
                        return nombre.contains(filtro);
                    })
                    .toList();
        }, result -> {
            java.util.List<ClienteDto> lista = result;

            DefaultTableModel m = view.getModel();
            TableUtils.clearModel(m);
            for (ClienteDto c : lista) {
                m.addRow(new Object[]{
                        c.idPersona(),
                        c.nombreCompleto(),
                        c.dni(),
                        c.telefono(),
                        c.direccion(),
                        c.estado()
                });
            }

            TableUtils.packColumns(view.getTabla());
            view.getScrollPane().revalidate();
            TableUtils.updateEmptyView(
                    view.getScrollPane(),
                    view.getTabla(),
                    view.getLblEmpty());
            view.updateButtons();
        }, ex -> {
            LOG.log(Level.SEVERE, "Error al buscar clientes", ex);
            ErrorHandler.handle(ex);
        });
    }

    /** Abre un diálogo de edición para la fila seleccionada y recarga los datos. */
    public void editarSeleccionado() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = view.getTabla().convertRowIndexToModel(row);
        Integer id = (Integer) view.getModel().getValueAt(modelRow, 0);
        AsyncTasks.busy(view, () -> UiContext.clienteSvc().obtener(id), dto -> {
            JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(view);
            DlgClienteEditar dlg = new DlgClienteEditar(owner);
            dlg.getTxtNombre().setText(dto.nombres());
            dlg.getTxtApellidos().setText(dto.apellidos());
            dlg.getTxtDni().setText(dto.dni());
            dlg.getTxtTelefono().setText(dto.telefono());
            dlg.getTxtDireccion().setText(dto.direccion());

            // eliminar el manejador "registrar" por defecto heredado de DlgClienteNuevo
            for (var al : dlg.getBtnGuardar().getActionListeners()) {
                dlg.getBtnGuardar().removeActionListener(al);
            }

            dlg.getBtnGuardar().addActionListener(ev -> {
                String nom  = dlg.getTxtNombre().getText().trim();
                String ape  = dlg.getTxtApellidos().getText().trim();
                String dni  = dlg.getTxtDni().getText().trim();
                String tel  = PhoneUtils.stripToDigits(dlg.getTxtTelefono().getText().trim());
                String dir  = dlg.getTxtDireccion().getText().trim();

                if (nom.isBlank() || ape.isBlank() || dni.isBlank() || dir.isBlank()) {
                    JOptionPane.showMessageDialog(dlg,
                            "Ingrese nombre, apellidos, DNI y dirección",
                            "Datos incompletos", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                AsyncTasks.busy(view, () -> {
                    ClienteCreateDto chg = new ClienteCreateDto(
                            nom, ape, dni, tel,
                            dir);
                    UiContext.clienteSvc().actualizar(id, chg);
                    return null;
                }, v -> {
                    dlg.dispose();
                    refresh();
                    raven.toast.Notifications.getInstance()
                            .show(raven.toast.Notifications.Type.SUCCESS,
                                    "Cliente actualizado");
                });
            });

            dlg.setVisible(true);
        });
    }

    /** Cambia el estado del cliente seleccionado a Activo. */
    public void activarSeleccionado() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0) return;
        int modelRow = view.getTabla().convertRowIndexToModel(row);
        Integer id = (Integer) view.getModel().getValueAt(modelRow, 0);
        cambiarEstado(id, EstadoNombre.ACTIVO.getNombre());
    }

    /** Desactiva al cliente seleccionado tras confirmación. */
    public void bajaSeleccionado() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0) return;
        int modelRow = view.getTabla().convertRowIndexToModel(row);
        Integer id = (Integer) view.getModel().getValueAt(modelRow, 0);
        if (DialogUtils.confirmAction(view.getScrollPane(),
                "¿Desactivar el cliente seleccionado?")) {
            cambiarEstado(id, EstadoNombre.INACTIVO.getNombre());
        }
    }

    private void cambiarEstado(Integer id, String nuevo) {
        AsyncTasks.busy(view, () -> {
            UiContext.clienteSvc().cambiarEstado(id, new CambiarEstadoDto(nuevo));
            return null;
        }, v -> refresh());
    }

    private void eliminar(Integer id) {
        AsyncTasks.busy(view, () -> {
            UiContext.clienteSvc().eliminar(id);
            return null;
        }, v -> {
            refresh();
            raven.toast.Notifications.getInstance()
                    .show(raven.toast.Notifications.Type.SUCCESS,
                            "Cliente eliminado");
        });
    }

    public java.util.List<String> getDependencias() { return dependencias; }

    /** Elimina el cliente seleccionado tras confirmación. */
    public void eliminarSeleccionado() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0) return;
        int modelRow = view.getTabla().convertRowIndexToModel(row);
        Integer id = (Integer) view.getModel().getValueAt(modelRow, 0);
        if (!dependencias.isEmpty()) {
            JOptionPane.showMessageDialog(view.getScrollPane(),
                    "No se puede eliminar, dependencias:\n- " + String.join("\n- ", dependencias),
                    "Dependencias", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (DialogUtils.confirmAction(view.getScrollPane(),
                "¿Eliminar permanentemente el cliente seleccionado?\nEsta acción es irreversible")) {
            eliminar(id);
        }
    }

    private void cargarDependencias() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0) {
            dependencias = java.util.List.of();
            return;
        }
        int modelRow = view.getTabla().convertRowIndexToModel(row);
        Integer id = (Integer) view.getModel().getValueAt(modelRow, 0);
        try {
            dependencias = UiContext.clienteSvc().obtenerDependencias(id);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al cargar dependencias", ex);
            ErrorHandler.handle(ex);
            dependencias = java.util.List.of();
        }
        view.updateButtons();
    }

    /** Abre un diálogo que muestra el historial de transacciones del cliente seleccionado. */
    public void mostrarHistorial() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0) return;
        int modelRow = view.getTabla().convertRowIndexToModel(row);
        Integer id = (Integer) view.getModel().getValueAt(modelRow, 0);
        AsyncTasks.busy(view, () -> UiContext.clienteSvc().obtener(id), dto -> {
            JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(view);
            DlgHistorialCliente dlg = new DlgHistorialCliente(owner);
            dlg.cargarCliente(dto);
            dlg.setVisible(true);
        }, ex -> {
            LOG.log(Level.SEVERE, "Error al mostrar historial", ex);
            ErrorHandler.handle(ex);
        });
    }

    /** Devuelve verdadero si el usuario actual tiene rol de administrador. */
    public boolean isAdmin() {
        return UiContext.getUsuarioActual() != null
                && RolNombre.fromNombre(UiContext.getUsuarioActual().rolNombre())
                        == RolNombre.ADMINISTRADOR;
    }
}

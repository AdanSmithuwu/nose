package com.comercialvalerio.presentation.controller.empleados;

import com.comercialvalerio.application.dto.EmpleadoCreateDto;
import com.comercialvalerio.application.dto.EmpleadoDto;
import com.comercialvalerio.application.dto.RolDto;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.ui.empleados.DlgEmpleadoEditar;
import com.comercialvalerio.presentation.ui.empleados.DlgEmpleadoNuevo;
import com.comercialvalerio.presentation.ui.empleados.DlgEmpleadoCredenciales;
import com.comercialvalerio.presentation.ui.empleados.FormEmpleados;
import com.comercialvalerio.application.dto.EmpleadoCredencialesDto;
import com.comercialvalerio.common.PhoneUtils;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.util.TableModelUtils;
import java.util.Locale;
import com.comercialvalerio.application.dto.RolNombre;
import com.comercialvalerio.application.dto.EstadoNombre;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.comercialvalerio.presentation.ui.util.DialogUtils;
import javax.swing.table.DefaultTableModel;

import java.util.List;

/** Controlador para {@link FormEmpleados}. */
public class EmpleadoController {

    private final FormEmpleados view;
    private static final Logger LOG = Logger.getLogger(EmpleadoController.class.getName());
    private java.util.List<String> dependencias = java.util.List.of();

    public EmpleadoController(FormEmpleados view) {
        this.view = view;
        this.view.getTabla().getSelectionModel().addListSelectionListener(e -> cargarDependencias());
    }

    /** Devuelve {@code true} si el usuario actual tiene rol de administrador. */
    public boolean isAdmin() {
        return UiContext.getUsuarioActual() != null
                && RolNombre.fromNombre(UiContext.getUsuarioActual().rolNombre()) == RolNombre.ADMINISTRADOR;
    }

    /** Recarga la tabla aplicando el filtro de búsqueda. */
    public void refresh() {
        String filtro = view.getTxtBuscar().getText().trim().toLowerCase(Locale.ROOT);
        String estado = (String) view.getCboEstado().getSelectedItem();
        AsyncTasks.busy(view, () -> {
            List<EmpleadoDto> lista = UiContext.empleadoSvc().listar();
            // Excluir solo al usuario administrador incorporado de la vista
            lista = lista.stream()
                    .filter(e -> e.usuario() == null ||
                            !e.usuario().equalsIgnoreCase("admin"))
                    .toList();
            if (!"Todos".equalsIgnoreCase(estado)) {
                String es = estado;
                lista = lista.stream()
                        .filter(e -> es.equalsIgnoreCase(e.estado()))
                        .toList();
            }
            if (!filtro.isBlank()) {
                String lf = filtro;
                lista = lista.stream().filter(e ->
                        e.nombres().toLowerCase(Locale.ROOT).contains(lf) ||
                        e.apellidos().toLowerCase(Locale.ROOT).contains(lf) ||
                        (e.dni() != null && e.dni().contains(lf)) ||
                        (e.telefono() != null && e.telefono().contains(lf))
                ).toList();
            }
            return lista;
        }, lista -> {
            String[] cols = {"ID","Nombre","DNI","Teléfono","Usuario","Rol","Estado"};
            DefaultTableModel m = TableModelUtils.createModel(
                    view.getTabla(), cols, new int[]{2,3}, 0);
            for (EmpleadoDto e : lista) {
                m.addRow(new Object[]{
                        e.idPersona(),
                        e.nombres()+" "+e.apellidos(),
                        e.dni(),
                        e.telefono(),
                        e.usuario(),
                        e.rolNombre(),
                        e.estado()
                });
            }
            TableUtils.packColumns(view.getTabla());
            view.getScrollPane().revalidate();
            TableUtils.updateEmptyView(
                    view.getScrollPane(),
                    view.getTabla(),
                    view.getLblEmpty());
            view.updateButtons();
        });
    }

    /** Abre el diálogo para nuevo empleado y luego actualiza la lista. */
    public void nuevo() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(view);
        new DlgEmpleadoNuevo(owner).setVisible(true);
        refresh();
        view.getTabla().requestFocusInWindow();
    }

    /** Abre un diálogo de edición para la fila seleccionada. */
    public void editarSeleccionado() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0) return;
        Integer id = (Integer) view.getTabla().getModel().getValueAt(row,0);
        AsyncTasks.busy(view, () -> {
            EmpleadoDto dto = UiContext.empleadoSvc().obtener(id);
            List<RolDto> roles = UiContext.rolSvc().listar();
            return new EditData(dto, roles);
        }, data -> {
            EmpleadoDto dto = data.empleado();
            List<RolDto> roles = data.roles();
            JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(view);
            DlgEmpleadoEditar dlg = new DlgEmpleadoEditar(owner);

            dlg.getTxtNombres().setText(dto.nombres());
            dlg.getTxtApellidos().setText(dto.apellidos());
            dlg.getTxtDni().setText(dto.dni());
            dlg.getTxtTelefono().setText(dto.telefono());

            for (RolDto r : roles) {
                dlg.getCboRol().addItem(r);
                if (dto.idRol()!=null && dto.idRol().equals(r.idRol())) {
                    dlg.getCboRol().setSelectedItem(r);
                }
            }

            dlg.getBtnCredenciales().addActionListener(ev -> {
                DlgEmpleadoCredenciales cd = new DlgEmpleadoCredenciales(owner);
                cd.getTxtUsuario().setText(dto.usuario());
                cd.getBtnGuardar().addActionListener(ev2 -> {
                    String usr = cd.getTxtUsuario().getText().trim();
                    String pwd = new String(cd.getTxtClave().getPassword()).trim();
                    if (!DialogUtils.confirmAction(cd,
                            "¿Actualizar credenciales del empleado?")) {
                        return;
                    }

                    String userChg = usr.equals(dto.usuario()) ? null : usr;
                    String pwdVal = pwd.isBlank() ? "" : pwd;

                    AsyncTasks.busy(view, () -> {
                        EmpleadoCredencialesDto uc = new EmpleadoCredencialesDto(userChg, pwdVal);
                        return UiContext.empleadoSvc().updateCredenciales(id, uc);
                    }, updated -> {
                        cd.dispose();
                        refresh();
                        if (updated.plainPassword() != null) {
                            JOptionPane.showMessageDialog(view,
                                    "Nueva contraseña: " + updated.plainPassword(),
                                    "Credenciales actualizadas",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    });
                });
                cd.setVisible(true);
            });

            dlg.getBtnGuardar().addActionListener(ev -> {
                String nom = dlg.getTxtNombres().getText().trim();
                String ape = dlg.getTxtApellidos().getText().trim();
                String tel = PhoneUtils.stripToDigits(dlg.getTxtTelefono().getText().trim());
                RolDto  rolSel = (RolDto) dlg.getCboRol().getSelectedItem();
                AsyncTasks.busy(view, () -> {
                    EmpleadoCreateDto chg = new EmpleadoCreateDto(
                            nom, ape, dto.dni(), tel,
                            rolSel.idRol(), null);
                    UiContext.empleadoSvc().actualizar(id, chg);
                    return null;
                }, v -> {
                    dlg.dispose();
                    refresh();
                    raven.toast.Notifications.getInstance()
                            .show(raven.toast.Notifications.Type.SUCCESS,
                                    "Empleado actualizado");
                });
            });

            dlg.setVisible(true);
            view.getTabla().requestFocusInWindow();
        });
    }

    /** Cambia el estado del empleado seleccionado a Activo. */
    public void activarSeleccionado() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0) return;
        Integer id = (Integer) view.getTabla().getModel().getValueAt(row,0);
        cambiarEstado(id, EstadoNombre.ACTIVO.getNombre());
    }

    /** Desactiva al empleado seleccionado tras confirmación. */
    public void bajaSeleccionado() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0) return;
        Integer id = (Integer) view.getTabla().getModel().getValueAt(row,0);
        Integer actualId = UiContext.getUsuarioActual()==null?null:
                UiContext.getUsuarioActual().idPersona();
        String actualUser = UiContext.getUsuarioActual()==null?null:
                UiContext.getUsuarioActual().usuario();
        boolean root = actualUser != null && actualUser.equalsIgnoreCase("admin");
        if (id.equals(actualId) && !root) {
            JOptionPane.showMessageDialog(view.getScrollPane(),
                    "No puede desactivar su propia cuenta",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (DialogUtils.confirmAction(view.getScrollPane(),
                "¿Desactivar el empleado seleccionado?")) {
            cambiarEstado(id, EstadoNombre.INACTIVO.getNombre());
        }
    }

    /** Helper to change employee status via REST and refresh table. */
    private void cambiarEstado(Integer id, String nuevo) {
        AsyncTasks.busy(view, () -> {
            UiContext.empleadoSvc().cambiarEstado(id, new CambiarEstadoDto(nuevo));
            return null;
        }, v -> {
            refresh();
        });
    }

    private void eliminar(Integer id) {
        AsyncTasks.busy(view, () -> {
            UiContext.empleadoSvc().eliminar(id);
            return null;
        }, v -> {
            refresh();
            raven.toast.Notifications.getInstance()
                    .show(raven.toast.Notifications.Type.SUCCESS,
                            "Empleado eliminado");
        });
    }

    public java.util.List<String> getDependencias() { return dependencias; }

    /** Elimina al empleado seleccionado tras la confirmación. */
    public void eliminarSeleccionado() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0) return;
        Integer id = (Integer) view.getTabla().getModel().getValueAt(row,0);
        boolean admin = isAdmin();
        if (!admin) {
            JOptionPane.showMessageDialog(view.getScrollPane(),
                    "Sólo un administrador puede eliminar empleados",
                    "Permiso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!dependencias.isEmpty()) {
            JOptionPane.showMessageDialog(view.getScrollPane(),
                    "No se puede eliminar, dependencias:\n- " + String.join("\n- ", dependencias),
                    "Dependencias", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (DialogUtils.confirmAction(view.getScrollPane(),
                "¿Eliminar permanentemente el empleado seleccionado?\nEsta acción es irreversible")) {
            eliminar(id);
        }
    }

    private void cargarDependencias() {
        int row = view.getTabla().getSelectedRow();
        if (row < 0) {
            dependencias = java.util.List.of();
            view.updateButtons();
            return;
        }
        Integer id = (Integer) view.getTabla().getModel().getValueAt(row,0);
        try {
            dependencias = UiContext.empleadoSvc().obtenerDependencias(id);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al cargar dependencias", ex);
            ErrorHandler.handle(ex);
            dependencias = java.util.List.of();
        }
        view.updateButtons();
    }
}

/** Helper record for edit dialog loading. */
record EditData(EmpleadoDto empleado, java.util.List<RolDto> roles) {}

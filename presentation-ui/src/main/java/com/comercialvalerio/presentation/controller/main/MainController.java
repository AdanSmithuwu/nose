package com.comercialvalerio.presentation.controller.main;

import com.comercialvalerio.presentation.ui.main.MainForm;
import com.comercialvalerio.presentation.ui.main.dependencies.Menu;
import com.comercialvalerio.presentation.ui.main.dependencies.MenuAction;
import com.comercialvalerio.presentation.ui.main.dependencies.MenuOption;
import com.comercialvalerio.presentation.ui.dashboard.FormDashboard;
import com.comercialvalerio.presentation.ui.clientes.FormClientes;
import com.comercialvalerio.presentation.ui.empleados.FormEmpleados;
import com.comercialvalerio.presentation.ui.categorias.FormCategorias;
import com.comercialvalerio.presentation.ui.reportes.FormReporteDiario;
import com.comercialvalerio.presentation.ui.reportes.FormReporteMensual;
import com.comercialvalerio.presentation.ui.reportes.FormReporteRotacion;
import com.comercialvalerio.presentation.ui.parametros.FormParametros;
import com.comercialvalerio.presentation.ui.bitacora.FormBitacoraLogin;
import com.comercialvalerio.presentation.launcher.ValerioApp;
import com.comercialvalerio.presentation.ui.historial.FormHistorialInventario;
import com.comercialvalerio.presentation.ui.pedidos.FormSeguimientoPedidos;
import com.comercialvalerio.presentation.ui.pedidos.FormPedidoEspecial;
import com.comercialvalerio.presentation.ui.productos.FormGestionInventario;
import com.comercialvalerio.presentation.ui.productos.FormGestionProductos;
import com.comercialvalerio.presentation.ui.ventas.FormSeguimientoVentas;
import com.comercialvalerio.presentation.ui.mantenimiento.FormMantenimiento;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.alertas.DlgAlertasStock;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.comercialvalerio.presentation.core.ErrorHandler;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Controlador responsable de las acciones del menú en {@link MainForm}.
 */
public class MainController {
    private final MainForm view;
    private final Map<MenuOption, Supplier<JComponent>> formSuppliers;
    private int lastIndex = MenuOption.DASHBOARD.index();
    private int lastSubIndex = 0;
    /** Diálogo reutilizable para mostrar alertas de stock. */
    private DlgAlertasStock alertDialog;

    public MainController(MainForm view) {
        this.view = view;
        this.formSuppliers = new EnumMap<>(MenuOption.class);
        formSuppliers.put(MenuOption.DASHBOARD, FormDashboard::new);
        formSuppliers.put(MenuOption.CLIENTES, FormClientes::new);
        formSuppliers.put(MenuOption.EMPLEADOS, FormEmpleados::new);
        formSuppliers.put(MenuOption.CATEGORIAS, FormCategorias::new);
        formSuppliers.put(MenuOption.PEDIDO_ESPECIAL, FormPedidoEspecial::new);
        formSuppliers.put(MenuOption.SEGUIMIENTO_VENTAS, FormSeguimientoVentas::new);
        formSuppliers.put(MenuOption.SEGUIMIENTO_PEDIDOS, FormSeguimientoPedidos::new);
        formSuppliers.put(MenuOption.PRODUCTOS, FormGestionProductos::new);
        formSuppliers.put(MenuOption.INVENTARIO, FormGestionInventario::new);
        formSuppliers.put(MenuOption.HISTORIAL_INVENTARIO, FormHistorialInventario::new);
        formSuppliers.put(MenuOption.REPORTE_DIARIO, FormReporteDiario::new);
        formSuppliers.put(MenuOption.REPORTE_MENSUAL, FormReporteMensual::new);
        formSuppliers.put(MenuOption.REPORTE_ROTACION, FormReporteRotacion::new);
        formSuppliers.put(MenuOption.BITACORA_ACCESOS, FormBitacoraLogin::new);
        formSuppliers.put(MenuOption.PARAMETROS, FormParametros::new);
        formSuppliers.put(MenuOption.MANTENIMIENTO, FormMantenimiento::new);
    }

    /**
     * Registra este controlador como oyente del menú.
     *
     * @param menu componente de menú de la vista
     */
    public void attachMenu(Menu menu) {
        menu.addMenuEvent(this::menuSelected);
    }

    private void menuSelected(int index, int subIndex, MenuAction action) {
        MenuOption option = MenuOption.fromIndex(index);
        if (option == MenuOption.LOGOUT) {
            if (DialogUtils.confirmAction(view, "¿Cerrar sesión?")) {
                ValerioApp.logout();
            } else {
                action.cancel();
                ValerioApp.setSelectedMenu(lastIndex, lastSubIndex);
            }
            return;
        }
        Supplier<JComponent> supplier = formSuppliers.get(option);
        if (supplier != null) {
            try {
                view.showForm(supplier.get());
                lastIndex = index;
                lastSubIndex = subIndex;
            } catch (RuntimeException ex) {
                ErrorHandler.handle(ex);
            }
        } else {
            view.showForm(placeholder("Opción no disponible"));
        }
    }

    private JComponent placeholder(String text) {
        JPanel p = new JPanel();
        p.add(new JLabel(text));
        return p;
    }

    /** Actualiza las etiquetas de información de usuario en la vista usando {@link UiContext}. */
    public void refreshUserInfo() {
        var usr = UiContext.getUsuarioActual();
        String nombre = (usr != null)
                ? usr.apellidos() + ", " + usr.nombres().charAt(0) + "."
                : "Invitado";
        String rol = (usr != null) ? "(" + usr.rolNombre() + ")" : "";
        view.updateUserInfo(nombre, rol);
    }

    /** Carga las alertas de stock pendientes y actualiza el contador en la vista. */
    public void refreshAlertas() {
        AsyncTasks.busy(view,
                () -> UiContext.alertaStockSvc().listarPendientes(),
                list -> view.updateAlertCount(list.size()));
    }

    /**
     * Muestra el diálogo de alertas con las existencias actuales.
     * Se reutiliza la misma instancia para no crearlo repetidamente.
     */
    public void mostrarAlertas() {
        if (alertDialog == null) {
            JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(view);
            alertDialog = new DlgAlertasStock(owner);
        }
        alertDialog.loadAlertas();
        alertDialog.setVisible(true);
        refreshAlertas();
    }

    /** Libera el diálogo de alertas si está creado. */
    public void disposeAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dispose();
            alertDialog = null;
        }
    }
}

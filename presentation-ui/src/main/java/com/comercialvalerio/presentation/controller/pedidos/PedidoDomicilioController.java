package com.comercialvalerio.presentation.controller.pedidos;

import com.comercialvalerio.presentation.ui.pedidos.FormPedido;
import com.comercialvalerio.presentation.ui.pedidos.FormPedidoDomicilio;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.application.dto.TipoPedido;
import com.comercialvalerio.presentation.util.NumberUtils;
import com.comercialvalerio.presentation.util.PriceUtils;
import javax.swing.table.DefaultTableModel;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Controlador para {@link FormPedidoDomicilio}. */
public class PedidoDomicilioController extends PedidoController {

    private static final Logger LOG =
            Logger.getLogger(PedidoDomicilioController.class.getName());

    public PedidoDomicilioController(FormPedido view) {
        super(view, true);
    }

    @Override
    public void agregarDetalle() {
        int row = view.getTblStock().getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "Seleccione un producto",
                    "Producto no seleccionado", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = view.getTblStock().convertRowIndexToModel(row);
        Integer prodId = (Integer) view.getTblStock().getModel().getValueAt(modelRow,0);
        var prod = productoCache.computeIfAbsent(
                prodId, id -> UiContext.productoSvc().obtener(id));
        java.math.BigDecimal stockDisponible = null;
        Object stockObj = view.getTblStock().getModel().getValueAt(modelRow,2);
        if (stockObj != null && !stockObj.toString().isBlank()) {
            try {
                stockDisponible = new java.math.BigDecimal(stockObj.toString());
            } catch (NumberFormatException ex) {
                stockDisponible = null;
                ErrorHandler.handle(new IllegalArgumentException("Stock invalido", ex));
            }
        }
        java.math.BigDecimal cantidad;
        String txt = view.getTxtCantidad().getText().trim();
        if (txt.isBlank()) cantidad = java.math.BigDecimal.ONE;
        else {
            try { cantidad = new java.math.BigDecimal(txt); }
            catch (NumberFormatException ex) {
                javax.swing.JOptionPane.showMessageDialog(view,
                        "Cantidad inválida",
                        "Dato inválido", javax.swing.JOptionPane.ERROR_MESSAGE);
                ErrorHandler.handle(new IllegalArgumentException("Cantidad inválida", ex));
                return;
            }
        }
        if (cantidad.scale() > 0 || cantidad.compareTo(java.math.BigDecimal.ONE) < 0
                || cantidad.compareTo(java.math.BigDecimal.valueOf(DbConstraints.MAX_CANTIDAD)) > 0) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "Cantidad inválida",
                    "Dato inválido", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (stockDisponible != null && cantidad.compareTo(stockDisponible) > 0) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "Stock insuficiente",
                    "Stock insuficiente", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (prod.stockActual() != null && cantidad.compareTo(prod.stockActual()) > 0) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "Stock insuficiente",
                    "Stock insuficiente", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.math.BigDecimal precio = PriceUtils.precioParaCantidad(prod, cantidad);

        for (int i = 0; i < detalles.size(); i++) {
            var d = detalles.get(i);
            if (d.idProducto().equals(prodId)) {
                java.math.BigDecimal nueva = d.cantidad().add(cantidad);
                if (stockDisponible != null && nueva.compareTo(stockDisponible) > 0) {
                    javax.swing.JOptionPane.showMessageDialog(view,
                            "Stock insuficiente",
                            "Stock insuficiente", javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (nueva.compareTo(java.math.BigDecimal.valueOf(DbConstraints.MAX_CANTIDAD)) > 0) {
                    javax.swing.JOptionPane.showMessageDialog(view,
                            "Cantidad máxima " + DbConstraints.MAX_CANTIDAD,
                            "Dato inválido", javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }
                precio = PriceUtils.precioParaCantidad(prod, nueva);
                detalles.set(i,
                        new com.comercialvalerio.application.dto.DetalleCreateDto(prodId, null, nueva, precio));
                refrescarTabla();
                updateStock(modelRow, cantidad.negate());
                return;
            }
        }
        if (stockDisponible != null && cantidad.compareTo(stockDisponible) > 0) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "Stock insuficiente",
                    "Stock insuficiente", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        precio = PriceUtils.precioParaCantidad(prod, cantidad);
        detalles.add(new com.comercialvalerio.application.dto.DetalleCreateDto(prodId, null, cantidad, precio));
        refrescarTabla();
        updateStock(modelRow, cantidad.negate());
    }

    @Override
    public void crear() {
        if (detalles.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "Agregue al menos un producto",
                    "Pedido sin productos", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        int rowCli = view.getTblClientes().getSelectedRow();
        if (rowCli < 0) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "Seleccione un cliente",
                    "Cliente no seleccionado", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRowCli = view.getTblClientes().convertRowIndexToModel(rowCli);
        Integer idCli = (Integer) view.getTblClientes().getModel().getValueAt(modelRowCli, 0);
        var cli = clientes.stream()
                .filter(c -> c.idPersona().equals(idCli))
                .findFirst().orElse(null);
        if (cli == null) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "Cliente inválido",
                    "Cliente inválido", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        String dir = cli.direccion();
        if (dir == null || dir.isBlank()) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "El cliente no tiene dirección registrada",
                    "Cliente sin dirección", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        AsyncTasks.busy(view, () -> {
            Integer emp = UiContext.getUsuarioActual() == null ? null
                    : UiContext.getUsuarioActual().idPersona();
            java.math.BigDecimal sub = java.math.BigDecimal.ZERO;
            for (var d : detalles) {
                sub = sub.add(d.cantidad().multiply(d.precioUnitario()));
            }
            sub = sub.setScale(DbConstraints.PRECIO_SCALE,
                               java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal desc = view.getChkValeGas().isSelected() ? descValeGas : java.math.BigDecimal.ZERO;
            desc = desc.setScale(DbConstraints.PRECIO_SCALE,
                                java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal total = sub.add(cargoReparto).subtract(desc)
                                          .setScale(DbConstraints.PRECIO_SCALE,
                                                    java.math.RoundingMode.HALF_UP);
            var dto = new com.comercialvalerio.application.dto.PedidoCreateDto(
                    sub, desc, cargoReparto, total,
                    observacion, emp, cli.idPersona(),
                    dir, TipoPedido.DOMICILIO,
                    view.getChkValeGas().isSelected(),
                    detalles
            );
            UiContext.pedidoSvc().crear(dto);
            return null;
        }, v -> {
            raven.toast.Notifications.getInstance()
                    .show(raven.toast.Notifications.Type.SUCCESS,
                            "Pedido registrado");
            cancelar();
        });
    }

    private void updateStock(int modelRow, java.math.BigDecimal delta) {
        DefaultTableModel tm =
                (DefaultTableModel) view.getTblStock().getModel();
        Object val = tm.getValueAt(modelRow, 2);
        if (val == null || val.toString().isBlank()) return;
        try {
            java.math.BigDecimal s =
                    new java.math.BigDecimal(val.toString()).add(delta);
            if (s.compareTo(java.math.BigDecimal.ZERO) < 0) {
                s = java.math.BigDecimal.ZERO;
            }
            tm.setValueAt(NumberUtils.formatPlain(s), modelRow, 2);
        } catch (NumberFormatException ex) {
            LOG.log(Level.WARNING, "Stock inválido", ex);
        }
    }
}

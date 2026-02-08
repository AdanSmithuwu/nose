package com.comercialvalerio.presentation.ui.pedidos;

import com.comercialvalerio.presentation.controller.pedidos.PedidoEspecialController;

/** Formulario utilizado para registrar transacciones de "Pedido Especial". */
public class FormPedidoEspecial extends FormPedido {

    public FormPedidoEspecial() {
        super(false);
        getChkValeGas().setVisible(false);
        initController(new PedidoEspecialController(this));
        // Se muestran subtotal y cargo para que la información sea consistente
        // con el resto de formularios de pedido
        getSubTotalPanel().setVisible(true);
        getCargoPanel().setVisible(true);
    }
}

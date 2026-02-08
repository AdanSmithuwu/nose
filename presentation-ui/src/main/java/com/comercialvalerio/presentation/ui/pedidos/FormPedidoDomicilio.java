package com.comercialvalerio.presentation.ui.pedidos;

import com.comercialvalerio.presentation.controller.pedidos.PedidoDomicilioController;

/** Formulario usado para registrar pedidos a domicilio regulares. */
public class FormPedidoDomicilio extends FormPedido {

    public FormPedidoDomicilio() {
        super(true);
        initController(new PedidoDomicilioController(this));
    }
}

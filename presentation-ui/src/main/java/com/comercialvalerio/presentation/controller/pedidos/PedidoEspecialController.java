package com.comercialvalerio.presentation.controller.pedidos;

import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.presentation.ui.pedidos.FormPedido;
import com.comercialvalerio.presentation.ui.pedidos.FormPedidoEspecial;
import com.comercialvalerio.presentation.util.PriceUtils;

/** Controlador para {@link FormPedidoEspecial}. */
public class PedidoEspecialController extends PedidoController {

    public PedidoEspecialController(FormPedido view) {
        super(view, false);
    }

    @Override
    protected boolean aplicaCargo() { return true; }

    @Override
    protected java.math.BigDecimal cantidadPorDefecto(ProductoDto p) {
        return java.math.BigDecimal.ONE;
    }

    @Override
    protected java.math.BigDecimal calcularPrecio(ProductoDto prod, java.math.BigDecimal cantidad) {
        return PriceUtils.precioMayorista(prod);
    }

    @Override
    protected String prefillCantidad(ProductoDto p) {
        return "";
    }
}

package com.comercialvalerio.domain.repository;

import java.util.List;

import com.comercialvalerio.domain.view.ProductoMasVendido;

/** Acceso a los datos de productos más vendidos. */
public interface ProductoMasVendidoRepository {

    /** Devuelve el ranking de productos más vendidos limitado al número dado. */
    List<ProductoMasVendido> top(int limite);
}

package com.comercialvalerio.domain.view;

/** Datos de un cliente con mayor n\u00famero de compras. */
public record ClienteFrecuenteView(Integer idCliente,
                                   String nombre,
                                   Integer numCompras) {}

package com.comercialvalerio.domain.repository;

import java.util.List;

import com.comercialvalerio.domain.view.ClienteFrecuenteView;

/** Ranking de clientes con más transacciones completadas. */
public interface ClienteFrecuenteRepository {
    List<ClienteFrecuenteView> top(int limite);
}

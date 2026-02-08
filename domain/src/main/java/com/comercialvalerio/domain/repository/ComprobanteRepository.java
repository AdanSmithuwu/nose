package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.Comprobante;

/* Comprobantes electrónicos (PDF + metadatos) */
public interface ComprobanteRepository {
    java.util.Optional<Comprobante> findById(Integer id);
    java.util.Optional<Comprobante> findByTransaccion(Integer idTransaccion);
    void save(Comprobante comprobante);
}

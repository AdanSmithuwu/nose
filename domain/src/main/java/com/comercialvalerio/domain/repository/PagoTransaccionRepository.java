package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.PagoTransaccion;
import java.util.List;

/* Pagos asociados a una transacción */
public interface PagoTransaccionRepository {
    List<PagoTransaccion> findByTransaccion(Integer idTransaccion);
    void save(PagoTransaccion pago);
    void delete(Integer idPago);
    /** Devuelve {@code true} cuando el cliente tiene pagos registrados. */
    boolean existsByCliente(Integer idCliente);
}

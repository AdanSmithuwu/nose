package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.Transaccion;
import java.time.LocalDateTime;
import java.util.List;

public interface TransaccionRepository {
    java.util.Optional<Transaccion> findById(Integer id);                 // Venta o Pedido
    List<Transaccion> findByRangoFecha(LocalDateTime d, LocalDateTime h);
    void actualizarEstado(Integer idTx, Integer idEstado, String motivo);
    /** Devuelve {@code true} si el empleado está referenciado por alguna transacción. */
    boolean existsByEmpleado(Integer idEmpleado);
}

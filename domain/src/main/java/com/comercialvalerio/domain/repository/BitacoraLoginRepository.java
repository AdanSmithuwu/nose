package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.BitacoraLogin;
import java.time.LocalDateTime;
import java.util.List;

/* Registro de accesos y autenticaciones */
public interface BitacoraLoginRepository {
    List<BitacoraLogin> findByEmpleado(Integer idEmpleado);
    List<BitacoraLogin> findByRangoFecha(LocalDateTime desde, LocalDateTime hasta);
    /** Consulta filtrando opcionalmente por el resultado del login. */
    List<BitacoraLogin> findByRangoFecha(LocalDateTime desde, LocalDateTime hasta,
                                         Boolean exitoso);
    /** Verifica si existe bitácora registrada por el empleado indicado. */
    boolean existsBitacoraByEmpleado(Integer idEmpleado);
    void save(BitacoraLogin evento);
    /** Elimina eventos anteriores a la fecha indicada. */
    void depurarAntiguos(LocalDateTime hasta);
}

package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.ParametroSistema;
import java.util.List;

/* Parámetros de configuración (clave-valor) editables en tiempo de ejecución */
public interface ParametroSistemaRepository {
    List<ParametroSistema> findAll();
    ParametroSistema findByClave(String clave);
    /** Valor entero de un parámetro o valor por defecto si falta o es inválido. */
    int getInt(String clave, int defecto);
    /** Actualiza el parámetro existente identificado por su clave. */
    void save(ParametroSistema parametro);
}

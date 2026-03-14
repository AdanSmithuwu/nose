package com.comercialvalerio.application.service;
import java.util.List;

import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.dto.EmpleadoCreateDto;
import com.comercialvalerio.application.dto.EmpleadoCredencialesDto;
import com.comercialvalerio.application.dto.EmpleadoDto;

public interface EmpleadoService {
    List<EmpleadoDto> listar();
    EmpleadoDto       obtener(Integer id);
    EmpleadoDto       crear(EmpleadoCreateDto datos);
    EmpleadoDto       actualizar(Integer id, EmpleadoCreateDto cambios);
    void              eliminar(Integer id);
    void cambiarEstado(Integer idEmpleado, CambiarEstadoDto dto);
    void resetClave   (Integer idEmpleado, String nuevaClave);
    EmpleadoDto updateCredenciales(Integer idEmpleado, EmpleadoCredencialesDto dto);
    /** Lista las entidades que referencian al empleado e impiden su eliminación. */
    List<String> obtenerDependencias(Integer idEmpleado);
    /* Auth */
    EmpleadoDto autenticar(String usuario, String plainPassword);

    /** Operación de cierre de sesión opcional para clientes REST. */
    default void logout() {}
}

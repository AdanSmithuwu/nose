package com.comercialvalerio.application.service;
import java.time.LocalDate;
import java.util.List;

import com.comercialvalerio.application.dto.ClienteCreateDto;
import com.comercialvalerio.application.dto.ClienteDto;
import com.comercialvalerio.application.dto.CambiarEstadoDto;
public interface ClienteService {
    /** Devuelve todos los clientes sin importar su estado. */
    List<ClienteDto> listar();
    /** Devuelve solo los clientes cuyo estado es 'Activo'. */
    List<ClienteDto> listarActivos();
    /** Devuelve clientes filtrados por nombre de estado. */
    List<ClienteDto> findByEstado(String nombre);
    ClienteDto       obtener(Integer id);
    ClienteDto       obtenerPorDni(String dni);
    List<ClienteDto> buscarPorNombre(String patron);
    List<ClienteDto> buscarPorTelefono(String numero);
    List<ClienteDto> listarPorRangoRegistro(LocalDate desde, LocalDate hasta);
    ClienteDto       registrar(ClienteCreateDto nuevo);
    ClienteDto       actualizar(Integer id, ClienteCreateDto cambios);
    void             eliminar(Integer id);
    void             cambiarEstado(Integer idCliente, CambiarEstadoDto dto);
    /** Devuelve una lista de entidades que impiden eliminar al cliente. */
    List<String>     obtenerDependencias(Integer idCliente);
}

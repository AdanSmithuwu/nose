package com.comercialvalerio.application.service.impl;

import java.time.LocalDateTime;

import com.comercialvalerio.application.dto.EmpleadoCredencialesDto;
import com.comercialvalerio.application.dto.EmpleadoDto;
import com.comercialvalerio.application.mapper.EmpleadoDtoMapper;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.model.Empleado;
import com.comercialvalerio.domain.repository.EmpleadoRepository;
import com.comercialvalerio.domain.security.PasswordHasher;
import com.comercialvalerio.domain.security.service.GeneradorCredenciales;

/** Utilidad para actualizar las credenciales de un empleado. */
public final class EmpleadoCredencialHelper {
    private EmpleadoCredencialHelper() {}

    public static EmpleadoDto updateCredenciales(EmpleadoRepository repo,
                                                 Empleado emp,
                                                 EmpleadoCredencialesDto dto,
                                                 PasswordHasher hasher,
                                                 GeneradorCredenciales generador,
                                                 EmpleadoDtoMapper mapper) {
        if ((dto.usuario() == null || dto.usuario().isBlank()) &&
            dto.plainPassword() == null) {
            throw new BusinessRuleViolationException(
                    "Debe proporcionar nuevo usuario y/o contraseña");
        }
        if (emp == null) {
            throw new EntityNotFoundException("Empleado no encontrado");
        }

        String nuevoUsuario = null;
        if (dto.usuario() != null && !dto.usuario().isBlank()) {
            emp.setUsuario(dto.usuario());
            nuevoUsuario = emp.getUsuario();
        }

        String nuevaClave = null;
        if (dto.plainPassword() != null) {
            if (dto.plainPassword().isBlank()) {
                nuevaClave = generador.generarClave(10);
            } else {
                nuevaClave = dto.plainPassword();
            }
            emp.resetClave(nuevaClave, hasher);
        }

        if (nuevoUsuario != null && nuevaClave == null) {
            emp.setFechaCambioClave(LocalDateTime.now());
        }

        if (nuevoUsuario != null || nuevaClave != null) {
            repo.updateCredenciales(emp.getIdPersona(),
                    nuevoUsuario,
                    nuevaClave != null ? emp.getHashClave() : null);
        }

        EmpleadoDto base = mapper.toDto(emp);
        return new EmpleadoDto(
                base.idPersona(), base.nombres(), base.apellidos(), base.dni(),
                base.telefono(), base.idRol(), base.rolNombre(),
                base.usuario(), base.estado(), base.fechaCambioClave(), nuevaClave);
    }
}

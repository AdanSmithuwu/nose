package com.comercialvalerio.application.service.impl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import com.comercialvalerio.application.dto.ParametroSistemaCreateDto;
import com.comercialvalerio.application.dto.ParametroSistemaDto;
import com.comercialvalerio.application.mapper.ParametroSistemaDtoMapper;
import com.comercialvalerio.application.service.ParametroSistemaService;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.model.Empleado;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.application.service.util.SecurityChecks;
import com.comercialvalerio.domain.model.ParametroSistema;
import com.comercialvalerio.domain.repository.EmpleadoRepository;
import com.comercialvalerio.domain.repository.ParametroSistemaRepository;
import com.comercialvalerio.domain.repository.ProductoRepository;
import com.comercialvalerio.domain.security.service.AutenticacionService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class ParametroSistemaServiceImpl implements ParametroSistemaService {

    private final ParametroSistemaRepository repoPar;
    private final EmpleadoRepository repoEmp;
    private final AutenticacionService authSvc;
    private final ProductoRepository repoProd;
@Inject
    ParametroSistemaDtoMapper mapper;

    @Inject
    public ParametroSistemaServiceImpl(ParametroSistemaRepository repoPar,
                                       EmpleadoRepository repoEmp,
                                       AutenticacionService authSvc,
                                       ProductoRepository repoProd) {
        this.repoPar = repoPar;
        this.repoEmp = repoEmp;
        this.authSvc = authSvc;
        this.repoProd = repoProd;
    }

    @Override
    public List<ParametroSistemaDto> listar() {
        return ServiceUtils.mapList(repoPar.findAll(), mapper::toDto);
    }

    @Override
    public ParametroSistemaDto obtener(String clave) {
        ParametroSistema p = repoPar.findByClave(clave.trim().toUpperCase(Locale.ROOT));
        if (p == null) throw new EntityNotFoundException("Parámetro no encontrado");
        return mapper.toDto(p);
    }

    @Override
    public ParametroSistemaDto guardar(String clave, ParametroSistemaCreateDto dto) {
        SecurityChecks.requireAdminRole();
        if (!clave.equalsIgnoreCase(dto.clave())) {
            throw new IllegalArgumentException("La clave del path y del body deben coincidir");
        }

        String key = clave.trim().toUpperCase(Locale.ROOT);

        if (dto.idEmpleado() == null)
            throw new IllegalArgumentException("idEmpleado obligatorio");
        Empleado emp = ServiceChecks.requireFound(
                repoEmp.findById(dto.idEmpleado()), "Empleado inexistente");

        ParametroSistema p = repoPar.findByClave(key);
        if (p == null) {
            throw new EntityNotFoundException("Par\u00e1metro no encontrado");
        }

        if (("MAX_INTENTOS_FALLIDOS".equals(key)
                || "MINUTOS_BLOQUEO_CUENTA".equals(key))
                && dto.valor().stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("valor debe ser entero");
        }

        p.setValor(dto.valor());
        p.setDescripcion(dto.descripcion());
        p.setEmpleado(emp);
        p.setActualizado(LocalDateTime.now());

        repoPar.save(p);
        if ("MAX_INTENTOS_FALLIDOS".equals(key)
                || "MINUTOS_BLOQUEO_CUENTA".equals(key)) {
            authSvc.refrescarLimites();
        }
        if ("MIN_CANTIDAD_MAYORISTA_HILO".equals(key)) {
            repoProd.actualizarMinMayoristaHilo(p.getValor().intValue());
        }
        return mapper.toDto(p);
    }
}

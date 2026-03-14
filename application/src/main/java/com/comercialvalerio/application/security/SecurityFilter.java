package com.comercialvalerio.application.security;

import com.comercialvalerio.application.security.HeaderUtils.EmpleadoHeaders;
import static com.comercialvalerio.application.security.HeaderNames.X_ID_EMPLEADO;
import com.comercialvalerio.domain.security.RequestContext;
import com.comercialvalerio.domain.model.RolNombre;
import com.comercialvalerio.domain.repository.EmpleadoRepository;
import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

/**
 * Establece {@link RequestContext} y un {@link SecurityContext} simple para la solicitud entrante.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOG = Logger.getLogger(SecurityFilter.class.getName());
    private static final Set<String> WRITE_METHODS =
            Set.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH);

    @jakarta.inject.Inject
    EmpleadoRepository repoEmp;

    /**
     * Normaliza una ruta eliminando las barras inclinadas finales.
     */
    private static String trimTrailingSlashes(String s) {
        return s == null ? "" : s.replaceAll("/+$", "");
    }

    @Override
    public void filter(ContainerRequestContext ctx) {
        EmpleadoHeaders headers = HeaderUtils.parseEmpleadoHeaders(ctx);
        boolean invalidId = headers.invalidId();
        if (invalidId) {
            LOG.log(Level.WARNING, "Invalid " + X_ID_EMPLEADO + " header: {0}", ctx.getHeaderString(X_ID_EMPLEADO));
            ctx.abortWith(Response.status(Response.Status.BAD_REQUEST)
                                 .entity(Map.of("error", "Cabecera " + X_ID_EMPLEADO + " malformada"))
                                 .build());
            return;
        }
        String rolRaw = headers.rol() != null ? headers.rol().trim() : RolNombre.EMPLEADO.getNombre();

        RolNombre rolNombre;
        try {
            rolNombre = RolNombre.fromNombre(rolRaw);
        } catch (IllegalArgumentException ex) {
            rolNombre = RolNombre.EMPLEADO;
        }

        boolean writeMethod = WRITE_METHODS.contains(ctx.getMethod());
        String clean = ctx.getUriInfo().getPath(false);
        boolean isLogin = isLoginPath(clean);
        LOG.log(Level.FINE, "Incoming path {0}, login request: {1}", new Object[]{clean, isLogin});
        if (writeMethod && !isLogin && headers.id() == null) {
            ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                                 .entity(Map.of("error", "Cabecera " + X_ID_EMPLEADO + " invalida"))
                                 .build());
            return;
        }

        if (!isLogin && headers.id() != null && !repoEmp.isActivo(headers.id())) {
            ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                                 .entity(Map.of("error", "Cuenta inactiva"))
                                 .build());
            return;
        }

        RequestContext.set(headers.id(), rolNombre.getNombre());
        SecurityContext base = ctx.getSecurityContext();
        RolNombre finalRol = rolNombre;
        ctx.setSecurityContext(new SecurityContext() {
            @Override public Principal getUserPrincipal() { return () -> "ui"; }
            @Override public boolean isUserInRole(String r) {
                try {
                    return finalRol == RolNombre.fromNombre(r);
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            }
            @Override public boolean isSecure() { return base.isSecure(); }
            @Override public String getAuthenticationScheme() { return "mock"; }
        });
    }

    private static final String LOGIN_PATH = "empleados/login";
    private static final String LOGIN_PATH_CLEAN = trimTrailingSlashes(LOGIN_PATH);

    /**
     * Verifica si la ruta corresponde al endpoint de inicio de sesión.
     */
    private static boolean isLoginPath(String path) {
        return trimTrailingSlashes(path).endsWith(LOGIN_PATH_CLEAN);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        RequestContext.clear();
    }
}

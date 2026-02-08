package com.comercialvalerio.domain.security;

/**
 * Mantiene la información de seguridad de la solicitud actual.
 *
 * <p>El contexto se almacena en variables {@link InheritableThreadLocal} para
 * propagarse automáticamente a tareas asíncronas en hilos hijos.
 */
public final class RequestContext {
    private static final InheritableThreadLocal<Integer> ID = new InheritableThreadLocal<>();
    private static final InheritableThreadLocal<String> ROLE = new InheritableThreadLocal<>();

    public static void set(Integer idEmpleado, String rol) {
        ID.set(idEmpleado);
        ROLE.set(rol != null ? rol.trim() : null);
    }

    public static Integer idEmpleado() { return ID.get(); }
    public static String rol() { return ROLE.get(); }

    public static void clear() {
        ID.remove();
        ROLE.remove();
    }

    /**
     * Abre un nuevo contexto que se restaurará a sus valores previos cuando el
     * {@link Scope} retornado sea cerrado.
     */
    public static Scope open(Integer idEmpleado, String rol) {
        return new Scope(idEmpleado, rol);
    }

    /**
     * Alcance de una petición. Utilizar en un bloque try-with-resources para
     * limpiar el contexto automáticamente al finalizar.
     */
    public static final class Scope implements AutoCloseable {
        private final Integer priorId;
        private final String priorRole;

        private Scope(Integer id, String rol) {
            this.priorId = idEmpleado();
            this.priorRole = rol();
            set(id, rol);
        }

        @Override
        public void close() {
            if (priorId == null && priorRole == null) {
                clear();
            } else {
                set(priorId, priorRole);
            }
        }
    }

    private RequestContext() {}
}

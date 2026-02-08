package com.comercialvalerio.infrastructure.transaction;

/**
 * Consultas auxiliares para el {@code SESSION_CONTEXT} de SQL Server.
 *
 * Estas sentencias nativas no pertenecen a ninguna entidad JPA en
 * particular. Por ello no se definieron como <i>named query</i>, ya que no
 * existe una clase de entidad a la cual asociarlas.
 * Manipulan el contexto de sesión del servidor, por lo que se mantienen
 * fuera de las entidades. La guía
 * {@code docs/persistence/jpql-guidelines.md} explica esta decisión.
 *
 * Se mantienen como constantes en esta clase para que
 * {@link TransactionManager} sea quien las ejecute al registrar y limpiar
 * el identificador del empleado en la sesión. Ningún otro componente las
 * utiliza.
 */
final class SessionContextQueries {
    /** Consulta para establecer el id del empleado en el session context. */
    static final String SET_CTX_SQL =
            "EXEC sp_set_session_context 'idEmpleado', ?1";
    /** Consulta para limpiar el id del empleado del session context. */
    static final String CLEAR_CTX_SQL =
            "EXEC sp_set_session_context 'idEmpleado', NULL";

    private SessionContextQueries() {}
}

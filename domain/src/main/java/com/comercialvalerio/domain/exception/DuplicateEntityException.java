package com.comercialvalerio.domain.exception;

/* Lanzada cuando se intenta crear o insertar una entidad duplicada */
public class DuplicateEntityException extends DataAccessException {
    public DuplicateEntityException(String mensaje) { super(mensaje, null); }
}

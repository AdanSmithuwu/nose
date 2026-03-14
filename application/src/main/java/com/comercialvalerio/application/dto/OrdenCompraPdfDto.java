package com.comercialvalerio.application.dto;

/** PDF de una orden de compra para descarga o envío. */
public record OrdenCompraPdfDto(
    String nombreArchivo,
    byte[] pdf
) {}

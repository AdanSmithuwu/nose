package com.comercialvalerio.application.dto;

/** PDF de un comprobante para descarga o envío. */
public record ComprobantePdfDto(
    String nombreArchivo,
    byte[] pdf
) {}

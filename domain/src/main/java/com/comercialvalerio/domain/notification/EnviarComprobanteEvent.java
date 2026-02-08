package com.comercialvalerio.domain.notification;

/** Evento lanzado para enviar un comprobante PDF por WhatsApp. */
public record EnviarComprobanteEvent(byte[] pdfBytes,
                                      String telefono,
                                      String fileName) {}

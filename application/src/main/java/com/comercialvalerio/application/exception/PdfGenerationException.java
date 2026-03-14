package com.comercialvalerio.application.exception;

/** Se lanza cuando ocurre un error al generar un PDF en la capa de aplicación. */
public class PdfGenerationException extends RuntimeException {
    public PdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.comercialvalerio.domain.exception;

/** Se lanza cuando ocurre un error al generar un documento PDF. */
public class PdfGenerationException extends RuntimeException {
    public PdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

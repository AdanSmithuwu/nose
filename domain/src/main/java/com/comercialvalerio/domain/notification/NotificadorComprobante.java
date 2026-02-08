package com.comercialvalerio.domain.notification;
import com.comercialvalerio.domain.exception.NotificationException;
import java.io.File;

/* Puerto / interfaz del dominio para enviar comprobantes. */
public interface NotificadorComprobante {
    void notificar(File pdf, String telefonoCliente) throws NotificationException;
}

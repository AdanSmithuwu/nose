package com.comercialvalerio.infrastructure.notification.whatsapp;
import com.comercialvalerio.domain.exception.NotificationException;
import com.comercialvalerio.infrastructure.config.AppConfig;
import com.comercialvalerio.common.PhoneUtils;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class TwilioWhatsAppSender {
    private static final Logger LOG =
        Logger.getLogger(TwilioWhatsAppSender.class.getName());
    private static final String COUNTRY_PREFIX = "51";
    private final String fromNumber;
    private final String templateSid;
    private final boolean enabled;

    public TwilioWhatsAppSender() {
        this.enabled = AppConfig.getBoolean("twilio.enabled");
        if (enabled) {
            // inicializa Twilio una sola vez
            Twilio.init(
               AppConfig.get("twilio.sid"),
               AppConfig.get("twilio.token")
            );
        } else {
            LOG.info("Twilio deshabilitado por configuración");
        }
        this.fromNumber  = AppConfig.get("twilio.number");
        this.templateSid = AppConfig.get("twilio.templateSid");
    }

    /** Indica si el envío de WhatsApp está habilitado. */
    public boolean isEnabled() {
        return enabled;
    }

    /* Envía un Template de WhatsApp con la URL. */
    public void sendTemplate(String toNumber, String urlDescarga) {
        if (!enabled) {
            LOG.log(Level.WARNING, "Se intentó enviar WhatsApp con Twilio deshabilitado");
            throw new NotificationException("El servicio de WhatsApp está deshabilitado", null);
        }
        String digits = PhoneUtils.stripToDigits(toNumber);
        if (digits == null || digits.isBlank()) {
            throw new NotificationException("Número de teléfono inválido", null);
        }
        if (!digits.startsWith(COUNTRY_PREFIX)) {
            digits = COUNTRY_PREFIX + digits;
        }
        try {
            Message.creator(
                new PhoneNumber("whatsapp:+" + digits),
                new PhoneNumber(fromNumber),
                "" // cuerpo en blanco; usamos template
            )
            .setContentSid(templateSid)
            .setContentVariables("{\"1\":\"" + urlDescarga + "\"}")
            .create();
            LOG.log(Level.INFO, "WhatsApp enviado a {0}", digits);
        } catch (com.twilio.exception.ApiException ex) {
            LOG.log(Level.SEVERE, "Error enviando WhatsApp", ex);
            throw new NotificationException(
                "No se pudo enviar el comprobante, verifique el número", ex);
        } catch (RuntimeException ex) {
            // captura cualquier otro error inesperado
            LOG.log(Level.SEVERE, "Error enviando WhatsApp", ex);
            throw new NotificationException("Error enviando WhatsApp", ex);
        }
    }
}

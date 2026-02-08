package com.comercialvalerio.infrastructure.notification;

import com.comercialvalerio.domain.notification.AlertService;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

@ApplicationScoped
public class ConsoleAlertService implements AlertService {

    private static final Logger LOG =
            Logger.getLogger(ConsoleAlertService.class.getName());

    @Override
    public void alertAdmin(String msg) {
        LOG.warning(msg);
    }
}

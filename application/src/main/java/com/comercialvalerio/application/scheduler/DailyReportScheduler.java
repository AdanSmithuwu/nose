package com.comercialvalerio.application.scheduler;

import com.comercialvalerio.application.service.ReporteService;
import com.comercialvalerio.common.time.TimeZoneProvider;
import com.comercialvalerio.domain.config.ConfigProvider;
import com.comercialvalerio.domain.notification.AlertService;
import com.comercialvalerio.domain.notification.EventBus;
import com.comercialvalerio.domain.notification.ReporteDiarioFallidoEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Inicia la generación del reporte diario a la medianoche. */
@ApplicationScoped
public class DailyReportScheduler {
    private static final Logger LOG = Logger.getLogger(DailyReportScheduler.class.getName());
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setName("daily-report");
        t.setDaemon(true);
        return t;
    });
    @Inject
    ConfigProvider config;
    @Inject
    ReporteService reporteService;
    @Inject
    EventBus eventBus;
    @Inject
    AlertService alertSvc;

    @PostConstruct
    void init() {
        if (!config.getBoolean("scheduler.reporteDiario.enabled")) {
            return;
        }
        long delay = secondsUntilNextMidnight();
        long period = TimeUnit.DAYS.toSeconds(1);
        executor.scheduleAtFixedRate(this::runTask, delay, period, TimeUnit.SECONDS);
    }

    private long secondsUntilNextMidnight() {
        ZoneId zone = TimeZoneProvider.zone();
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDateTime next = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, next).getSeconds();
    }

    private void runTask() {
        LocalDate fecha = LocalDate.now(TimeZoneProvider.zone()).minusDays(1);
        try {
            reporteService.generarDiario(fecha);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al generar el reporte diario", ex);
            if (eventBus != null) {
                eventBus.publish(new ReporteDiarioFallidoEvent(fecha, ex))
                        .exceptionally(err -> {
                            LOG.log(Level.SEVERE,
                                    "Error enviando notificacion de falla de reporte", err);
                            Optional.ofNullable(alertSvc)
                                    .ifPresent(svc -> svc.alertAdmin(
                                            "No se pudo notificar falla del reporte diario"));
                            return null;
                        });
            } else if (alertSvc != null) {
                alertSvc.alertAdmin("Error al generar el reporte diario");
            }
        }
    }

    @PreDestroy
    void shutdown() {
        // Detiene la planificación y espera brevemente a que termine la tarea
        // en ejecución. Si no finaliza en cinco segundos se interrumpe.
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

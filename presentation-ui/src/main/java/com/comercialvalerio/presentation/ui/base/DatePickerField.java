package com.comercialvalerio.presentation.ui.base;

import java.time.LocalDate;

import javax.swing.JFormattedTextField;

import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.formdev.flatlaf.FlatClientProperties;

import raven.datetime.DatePicker;

/**
 * Campo de texto conectado a {@link DatePicker} que muestra un calendario emergente.
 */
public class DatePickerField extends JFormattedTextField {

    private final DatePicker picker = new DatePicker();

    public DatePickerField() {
        picker.setCloseAfterSelected(true);
        picker.setEditorValidation(true);
        picker.setValidationOnNull(true);
        picker.setAnimationEnabled(true);
        picker.setStartWeekOnMonday(true);
        putClientProperty(FlatClientProperties.STYLE,
                "arc:" + UIStyle.ARC_DIALOG + ";");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        picker.setEditor(this);
    }

    /**
     * Devuelve la fecha seleccionada o {@code null} si no se eligió ninguna.
     */
    public LocalDate getDate() {
        return picker.getSelectedDate();
    }

    /**
     * Establece la fecha seleccionada en el selector.
     */
    public void setDate(LocalDate date) {
        picker.setSelectedDate(date);
    }

    /**
     * Define el formato de visualización de la fecha.
     */
    public void setDateFormat(String pattern) {
        picker.setDateFormat(pattern);
    }

    /**
     * Agrega un escucha notificado cuando se elige una fecha.
     */
    public void addDateSelectionListener(raven.datetime.event.DateSelectionListener l) {
        picker.addDateSelectionListener(l);
    }

    /**
     * Mueve el selector a la fecha actual y la selecciona.
     */
    public void now() {
        picker.now();
    }

    /**
     * Expone la instancia de {@link DatePicker} para opciones avanzadas.
     */
    public DatePicker getPicker() {
        return picker;
    }
}

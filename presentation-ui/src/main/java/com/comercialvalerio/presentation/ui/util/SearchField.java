package com.comercialvalerio.presentation.ui.util;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

/**
 * Componente reutilizable de búsqueda que combina un campo de texto y un botón
 * para limpiar. El ícono de búsqueda se muestra dentro del campo.
 */
public class SearchField extends JPanel {

    /** Altura predeterminada del componente en píxeles. */
    public static final int DEFAULT_HEIGHT = 40;

    private final JTextField textField = new JTextField();
    private final JButton clearButton = new JButton();
    // Ícono de búsqueda mostrado dentro del campo de texto
    private static final FlatSVGIcon SEARCH_ICON = new FlatSVGIcon(
            "com/comercialvalerio/presentation/ui/icon/svg/search.svg", 0.8f);

    private int arc = UIStyle.ARC_DEFAULT;
    private String borderColor = UIStyle.HEX_GRAY;

    public SearchField() {
        this("", DEFAULT_HEIGHT);
    }

    public SearchField(String placeholder) {
        this(placeholder, DEFAULT_HEIGHT);
    }

    public SearchField(String placeholder, int height) {
        this(placeholder, height, UIStyle.ARC_DEFAULT, UIStyle.HEX_GRAY);
    }

    public SearchField(String placeholder, int height, int arc, String borderColor) {
        this.arc = arc;
        this.borderColor = borderColor;
        setOpaque(false);
        setLayout(new MigLayout("insets 0, gap 0", "[grow,fill][30!]", "[fill]"));

        textField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        KeyUtils.setTooltipAndMnemonic(textField, KeyEvent.VK_F3, "Buscar");
        applyStyle();

        clearButton.setIcon(new FlatSVGIcon(
                "com/comercialvalerio/presentation/ui/icon/svg/close.svg", 0.8f));
        clearButton.putClientProperty(FlatClientProperties.STYLE,
                "background:null; hoverBackground:" + UIStyle.HEX_LIGHT_BLUE);
        KeyUtils.setTooltipAndMnemonic(clearButton, KeyEvent.VK_K, "Limpiar");

        clearButton.setVisible(false);
        DocumentListener toggle = new DocumentListener() {
            private void update() {
                clearButton.setVisible(!textField.getText().isEmpty());
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        };
        textField.getDocument().addDocumentListener(toggle);

        // se muestra el ícono de búsqueda dentro del campo y se omite la etiqueta
        textField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, SEARCH_ICON);

        SwingUtilities.invokeLater(() ->
                KeyUtils.registerKeyAction(this,
                        KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_DOWN_MASK),
                        clearButton::doClick));

        add(textField, "growx, h " + height + "!");
        add(clearButton, "h " + height + "!, w 30!");
    }

    private void applyStyle() {
        textField.putClientProperty(FlatClientProperties.STYLE,
                "arc:" + arc + "; borderColor:" + borderColor +
                        "; minimumWidth:" + UIStyle.SEARCH_FIELD_MIN_WIDTH);
    }

    /**
     * Permite cambiar el radio y color del borde después de construir.
     */
    public void setArcAndBorder(int arc, String borderColor) {
        this.arc = arc;
        this.borderColor = borderColor;
        applyStyle();
    }

    /** Devuelve el campo de texto subyacente para configuración adicional. */
    public JTextField getTextField() {
        return textField;
    }

    /** Conveniencia para leer el texto directamente. */
    public String getText() {
        return textField.getText();
    }

    /** Agrega un listener al botón de limpiar. */
    public void addClearActionListener(ActionListener l) {
        clearButton.addActionListener(l);
    }

    /** Agrega un ActionListener al campo de texto. */
    public void addActionListener(ActionListener l) {
        textField.addActionListener(l);
    }
}

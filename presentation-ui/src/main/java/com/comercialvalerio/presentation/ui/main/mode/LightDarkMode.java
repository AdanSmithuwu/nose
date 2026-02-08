package com.comercialvalerio.presentation.ui.main.mode;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.comercialvalerio.presentation.ui.util.UserPrefs;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import javax.swing.UIManager;
import javax.swing.KeyStroke;
import java.beans.PropertyChangeListener;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 *
 */
public class LightDarkMode extends JPanel {

    public void setMenuFull(boolean menuFull) {
        this.menuFull = menuFull;
        if (menuFull) {
            buttonLight.setVisible(true);
            buttonDark.setVisible(true);
            buttonLightDark.setVisible(false);
        } else {
            buttonLight.setVisible(false);
            buttonDark.setVisible(false);
            buttonLightDark.setVisible(true);
        }
    }

    private boolean menuFull = true;

    public LightDarkMode() {
        init();
        // Mantiene los íconos sincronizados con el Look and Feel actual
        PropertyChangeListener l = e -> {
            if ("lookAndFeel".equals(e.getPropertyName())) {
                checkStyle();
            }
        };
        UIManager.addPropertyChangeListener(l);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0),
                () -> changeMode(!FlatLaf.isLafDark()));
    }

    private void init() {
        setBorder(new EmptyBorder(2, 2, 2, 2));
        setLayout(new LightDarkModeLayout());
        putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:" + UIStyle.ARC_ROUND + ";"
                + "background:$Menu.lightdark.background");
        buttonLight = new JButton("Claro", new FlatSVGIcon("com/comercialvalerio/presentation/ui/main/mode/light.svg"));
        buttonDark = new JButton("Oscuro", new FlatSVGIcon("com/comercialvalerio/presentation/ui/main/mode/dark.svg"));
        buttonLightDark = new JButton();
        KeyUtils.setTooltipAndMnemonic(buttonLightDark, KeyEvent.VK_F6, "Cambiar modo claro/oscuro");
        buttonLightDark.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:" + UIStyle.ARC_ROUND + ";"
                + "background:$Menu.lightdark.button.background;"
                + "foreground:$Menu.foreground;"
                + "focusWidth:0;"
                + "borderWidth:0;"
                + "innerFocusWidth:0");
        buttonLightDark.addActionListener((ActionEvent e) -> {
            changeMode(!FlatLaf.isLafDark());
        });
        checkStyle();
        buttonDark.addActionListener((ActionEvent e) -> {
            changeMode(true);
        });
        buttonLight.addActionListener((ActionEvent e) -> {
            changeMode(false);
        });

        add(buttonLight);
        add(buttonDark);
        add(buttonLightDark);
    }

    public void changeMode(boolean dark) {
        if (FlatLaf.isLafDark() != dark) {
            UserPrefs.setDarkMode(dark);
            if (dark) {
                EventQueue.invokeLater(() -> {
                    FlatAnimatedLafChange.showSnapshot();
                    FlatMacDarkLaf.setup();
                    FlatLaf.updateUI();
                    checkStyle();
                    FlatAnimatedLafChange.hideSnapshotWithAnimation();
                });
            } else {
                EventQueue.invokeLater(() -> {
                    FlatAnimatedLafChange.showSnapshot();
                    FlatMacLightLaf.setup();
                    FlatLaf.updateUI();
                    checkStyle();
                    FlatAnimatedLafChange.hideSnapshotWithAnimation();
                });
            }
        }
    }

    private void checkStyle() {
        boolean isDark = FlatLaf.isLafDark();
        addStyle(buttonLight, !isDark);
        addStyle(buttonDark, isDark);
        if (isDark) {
            buttonLightDark.setIcon(new FlatSVGIcon("com/comercialvalerio/presentation/ui/main/mode/dark.svg"));
        } else {
            buttonLightDark.setIcon(new FlatSVGIcon("com/comercialvalerio/presentation/ui/main/mode/light.svg"));
        }
    }

    private void addStyle(JButton button, boolean style) {
        if (style) {
            button.putClientProperty(FlatClientProperties.STYLE, ""
                    + "arc:" + UIStyle.ARC_ROUND + ";"
                    + "background:$Menu.lightdark.button.background;"
                    + "foreground:$Menu.foreground;"
                    + "focusWidth:0;"
                    + "borderWidth:0;"
                    + "innerFocusWidth:0");
        } else {
            button.putClientProperty(FlatClientProperties.STYLE, ""
                    + "arc:" + UIStyle.ARC_ROUND + ";"
                    + "background:$Menu.lightdark.button.background;"
                    + "foreground:$Menu.foreground;"
                    + "focusWidth:0;"
                    + "borderWidth:0;"
                    + "innerFocusWidth:0;"
                    + "background:null");
        }
    }

    private JButton buttonLight;
    private JButton buttonDark;
    private JButton buttonLightDark;

    private class LightDarkModeLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                return new Dimension(5, buttonDark.getPreferredSize().height + (menuFull ? 0 : 5));
            }
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                return new Dimension(0, 0);
            }
        }

        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets insets = parent.getInsets();
                int x = insets.left;
                int y = insets.top;
                int gap = 5;
                int width = parent.getWidth() - (insets.left + insets.right);
                int height = parent.getHeight() - (insets.top + insets.bottom);
                int buttonWidth = (width - gap) / 2;
                if (menuFull) {
                    buttonLight.setBounds(x, y, buttonWidth, height);
                    buttonDark.setBounds(x + buttonWidth + gap, y, buttonWidth, height);
                } else {
                    buttonLightDark.setBounds(x, y, width, height);
                }
            }
        }
    }
}

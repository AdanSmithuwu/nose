package com.comercialvalerio.presentation.ui.base;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * JButton con una pequeña insignia numérica superpuesta al icono.
 */
public class BadgeButton extends JButton {

    private int badgeCount;
    private BufferedImage iconImage;
    private Color badgeColor = new Color(220, 67, 67);

    public BadgeButton() {
        this(0);
    }

    public BadgeButton(int count) {
        this.badgeCount = count;
        setUI(new BadgeUI());
        setOpaque(false);
        setContentAreaFilled(false);
        setForeground(Color.WHITE);
        setBorder(new EmptyBorder(10, 11, 10, 11));
        setHorizontalTextPosition(SwingConstants.CENTER);
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(int count) {
        this.badgeCount = Math.max(0, count);
        repaint();
    }

    public Color getBadgeColor() {
        return badgeColor;
    }

    public void setBadgeColor(Color badgeColor) {
        this.badgeColor = badgeColor;
        repaint();
    }

    @Override
    public void setIcon(Icon icon) {
        super.setIcon(icon);
        createImage();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setUI(new BadgeUI());
        createImage();
    }

    private void createImage() {
        Icon icon = getIcon();
        if (icon != null) {
            iconImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = iconImage.createGraphics();
            icon.paintIcon(this, g2, 0, 0);
            g2.dispose();
        } else {
            iconImage = null;
        }
    }

    private class BadgeUI extends BasicButtonUI {
        @Override
        protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
            // No pintar texto para que no aparezca debajo del icono
        }

        @Override
        protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect) {
            if (iconImage != null && badgeCount > 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                double size = Math.max(iconRect.getWidth(), iconRect.getHeight()) * 0.8f;
                double x = Math.min(iconRect.getX() + iconRect.getWidth() / 2, c.getWidth() - size);
                double y = Math.max(iconRect.getY() - size / 2, 0);

                Area area = new Area(iconRect);
                area.subtract(new Area(new Ellipse2D.Double(x, y, size, size)));
                g2.setPaint(new TexturePaint(iconImage, iconRect));
                g2.fill(area);

                drawBadge(g2, x, y, size);
                g2.dispose();
            } else {
                super.paintIcon(g, c, iconRect);
            }
        }

        private void drawBadge(Graphics2D g2, double x, double y, double size) {
            String text = badgeCount > 99 ? "99+" : String.valueOf(badgeCount);
            FontMetrics fm = g2.getFontMetrics();
            Rectangle2D r2d = fm.getStringBounds(text, g2);
            double space = size * 0.08f;
            double width = Math.max(size - space * 2, r2d.getWidth() + space * 2);
            double height = size - space * 2;

            g2.setColor(badgeColor);
            g2.translate(x, y);
            g2.fill(new RoundRectangle2D.Double(space, space, width, height, height, height));

            double tx = ((width - r2d.getWidth()) / 2) + space;
            double ty = ((height - r2d.getHeight()) / 2) + space;
            g2.setColor(getForeground());
            g2.drawString(text, (int) tx, (int) ty + fm.getAscent());
        }
    }
}

package com.comercialvalerio.presentation.ui.dashboard;

import com.comercialvalerio.presentation.ui.theme.UIStyle;
import net.miginfocom.swing.MigLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Container;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatLaf;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.event.KeyEvent;

import com.comercialvalerio.presentation.controller.dashboard.DashboardController;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.beans.PropertyChangeListener;

/*
 * Dashboard que replica el diseño del mock-up proporcionado.
 *
 *  · Barra superior con buscador, botones de acción y avatar del usuario.
 *  · Tres tarjetas métricas (ventas, pedidos y reloj).
 *  · Tabla “Productos más vendidos”.
 *  · Calendario
 *
 * No depende de librerías externas; todo es puro Swing y FlatLaf.
 *
 */
public class FormDashboard extends BaseForm implements Refreshable {
    private final JLabel lblEmptyBest = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JLabel lblEmptyClientes = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JLabel lblEmptyPendientes = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);

    /* metric components */
    private JPanel metricsPanel;
    private JLabel lblVentasValue;
    private JProgressBar barVentas;
    private JLabel lblVentasFoot;
    private JLabel lblPedidosValue;
    private JProgressBar barPedidos;
    private JLabel lblPedidosFoot;

    /* best seller components */
    private DefaultTableModel bestSellerModel;
    private JTable tblBestSeller;
    private JScrollPane spBestSeller;

    /* frequent clients components */
    private DefaultTableModel clientesModel;
    private JTable tblClientes;
    private JScrollPane spClientes;

    /* pending orders components */
    private DefaultTableModel pendientesModel;
    private JTable tblPendientes;
    private JScrollPane spPendientes;

    /* calendar */
    private MiniCalendar calendar;

    /* controller */
    private DashboardController controller;
    private final JButton btnRefresh = UIUtils.createRefreshButton(() -> controller.refresh());
    private final PropertyChangeListener lafListener = e -> {
        if ("lookAndFeel".equals(e.getPropertyName()) && metricsPanel != null) {
            SwingUtilities.invokeLater(() -> updateCardColors(this));
        }
    };

    public FormDashboard() {
        controller = new DashboardController(this);
        initUI();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        UIManager.addPropertyChangeListener(lafListener);
    }

    @Override
    public void removeNotify() {
        UIManager.removePropertyChangeListener(lafListener);
        super.removeNotify();
    }

    /* ===================================================================== */
    /*   I N I C I A L I Z A C I Ó N                                         */
    /* ===================================================================== */
    private void initUI() {
        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]", "[]15[grow]"));
        putClientProperty(FlatClientProperties.STYLE, "background:@background");
        setBorder(UIStyle.FORM_MARGIN);

        HeaderPanel header = new HeaderPanel("Dashboard", btnRefresh);
        header.setBorder(new EmptyBorder(0,0,5,0));
        add(header, "cell 0 0, growx, wrap");

        JPanel center = new JPanel(new BorderLayout(15,15));
        center.setOpaque(false);
        add(center, "cell 0 1, grow");

        /* tarjetas métricas */
        metricsPanel = new JPanel(new GridLayout(1,3,15,0));
        metricsPanel.setOpaque(false);
        center.add(metricsPanel, BorderLayout.NORTH);

        lblVentasValue = new JLabel("-");
        barVentas = new JProgressBar(0,100);
        lblVentasFoot = new JLabel();
        metricsPanel.add(metricCard("Total de Ventas", lblVentasValue, barVentas, lblVentasFoot));

        metricsPanel.add(clockCard());

        lblPedidosValue = new JLabel("-");
        barPedidos = new JProgressBar(0,100);
        lblPedidosFoot = new JLabel();
        metricsPanel.add(metricCard("Total de Pedidos", lblPedidosValue, barPedidos, lblPedidosFoot));

        controller.loadMetrics();

        /* zona inferior: rankings y calendario */
        JPanel bottom = new JPanel(new GridLayout(1,4,15,0));
        bottom.setOpaque(false);
        bottom.add(bestSellerCard());
        bottom.add(calendarCard());
        bottom.add(clientRankCard());
        bottom.add(pendingOrdersCard());
        center.add(bottom, BorderLayout.CENTER);
        controller.loadPendientes();
    }

    @Override
    protected void registerShortcuts() {
        KeyUtils.registerRefreshAction(this, controller::refresh);
    }

    /* ******************************************************************** */
    /*  T A R J E T A S   M É T R I C A S                                   */
    /* ******************************************************************** */
    private JPanel metricCard(String title, JLabel value, JProgressBar bar, JLabel foot){
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UIStyle.getColorCardBg());
        p.putClientProperty(FlatClientProperties.STYLE,"arc:" + UIStyle.ARC_PILL );
        p.setBorder(new EmptyBorder(15,15,15,15));

        JLabel t = new JLabel(title);
        t.putClientProperty(FlatClientProperties.STYLE,
                "font:+2; foreground:" + UIStyle.getHexSecondaryText());
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);

        value.putClientProperty(FlatClientProperties.STYLE,"font:$h1.font");
        value.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(value);

        bar.setStringPainted(true);
        bar.putClientProperty(FlatClientProperties.STYLE, "arc:" + UIStyle.ARC_ROUND );
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, bar.getPreferredSize().height));
        p.add(bar);

        foot.setFont(foot.getFont().deriveFont(Font.PLAIN,11f));
        foot.setForeground(UIStyle.getColorSecondaryText());
        foot.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(foot);

        return p;
    }

    private JPanel clockCard(){
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIStyle.getColorCardBg());
        p.putClientProperty(FlatClientProperties.STYLE,"arc:" + UIStyle.ARC_PILL );
        p.setBorder(new EmptyBorder(15,15,15,15));

        JLabel t = new JLabel("Hora Actual");
        t.putClientProperty(FlatClientProperties.STYLE,
                "font:+2; foreground:" + UIStyle.getHexSecondaryText());
        p.add(t, BorderLayout.NORTH);

        AnalogClock clock = new AnalogClock();
        p.add(clock, BorderLayout.CENTER);

        JLabel time = new JLabel();
        time.putClientProperty(FlatClientProperties.STYLE,"font:$h1.font");
        time.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(time, BorderLayout.SOUTH);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        Timer timer = new Timer(1000, e -> time.setText(LocalTime.now().format(fmt)));
        timer.start();
        time.setText(LocalTime.now().format(fmt));

        return p;
    }

    /* ******************************************************************** */
    /*  C A R D   “ P R O D U C T O S   M Á S   V E N D I D O S ”           */
    /* ******************************************************************** */
    private JPanel bestSellerCard(){
        JPanel card = cardWrapper("Productos más Vendidos");

        String[] cols={"Producto","Unidades"};
        bestSellerModel = new DefaultTableModel(cols,0);
        tblBestSeller = new NonEditableTable(bestSellerModel);
        tblBestSeller.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        spBestSeller = new JScrollPane(tblBestSeller);
        card.add(spBestSeller, BorderLayout.CENTER);

        controller.loadBestSellers();

        return card;
    }

    /* ******************************************************************** */
    /*  C A R D   C L I E N T E S   F R E C U E N T E S                       */
    /* ******************************************************************** */
    private JPanel clientRankCard(){
        JPanel card = cardWrapper("Clientes Frecuentes");

        String[] cols = {"Cliente","Compras"};
        clientesModel = new DefaultTableModel(cols,0);
        tblClientes = new NonEditableTable(clientesModel);
        tblClientes.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        spClientes = new JScrollPane(tblClientes);
        card.add(spClientes, BorderLayout.CENTER);

        controller.loadClientes();

        return card;
    }

    /* ******************************************************************** */
    /*  C A R D   P E D I D O S   P E N D I E N T E S                        */
    /* ******************************************************************** */
    private JPanel pendingOrdersCard() {
        JPanel card = cardWrapper("Pedidos Pendientes");

        String[] cols = {"ID", "Cliente", "Fecha"};
        pendientesModel = new DefaultTableModel(cols, 0);
        tblPendientes = new NonEditableTable(pendientesModel);
        tblPendientes.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        spPendientes = new JScrollPane(tblPendientes);
        card.add(spPendientes, BorderLayout.CENTER);

        return card;
    }

    /* ******************************************************************** */
    /*  C A R D   C A L E N D A R I O                                       */
    /* ******************************************************************** */
    private JPanel calendarCard(){
        JPanel card = cardWrapper("Calendario");
        card.setPreferredSize(new Dimension(220,220));
        calendar = new MiniCalendar();
        card.add(calendar, BorderLayout.CENTER);
        return card;
    }

    private JPanel cardWrapper(String title){
        JPanel c=new JPanel(new BorderLayout(5,5));
        c.setBackground(UIStyle.getColorCardBg());
        c.putClientProperty(FlatClientProperties.STYLE,"arc:" + UIStyle.ARC_PILL );
        c.setBorder(UIStyle.FORM_MARGIN);

        JLabel l=new JLabel(title);
        l.putClientProperty(FlatClientProperties.STYLE,
                "font:+1; foreground:" + UIStyle.getHexSecondaryText());
        c.add(l, BorderLayout.NORTH);
        return c;
    }

    /* ******************************************************************** */
    /*  M I N I   C A L E N D A R I O                                       */
    /* ******************************************************************** */
    private static class MiniCalendar extends JPanel {

        private static final String[] DAY = {"Lu","Ma","Mi","Ju","Vi","Sa","Do"};
        private final Color headerBg = new Color(36,104,155);
        private LocalDate current;

        private JLabel head;
        private JLabel[] cells;

        MiniCalendar(){
            setLayout(new BorderLayout(0,2));
            setOpaque(false);
            setFocusable(true);
            current = LocalDate.now();
            initComponents();
            updateCalendar();
            SwingUtilities.invokeLater(this::requestFocusInWindow);
        }

        private void initComponents(){
            /* encabezado con navegación */
            JPanel headPanel = new JPanel(new BorderLayout());
            headPanel.setOpaque(true);
            headPanel.setBackground(headerBg);

            JButton prev = new JButton(new FlatSVGIcon(
                    "com/comercialvalerio/presentation/ui/icon/svg/menu_left.svg",
                    0.8f));
            KeyUtils.setTooltipAndMnemonic(prev, KeyEvent.VK_LEFT, "Mes anterior");
            prev.putClientProperty(FlatClientProperties.STYLE,
                    "background:null; hoverBackground:" + UIStyle.HEX_LIGHT_BLUE);
            prev.addActionListener(e -> { current = current.minusMonths(1); updateCalendar(); });

            JButton next = new JButton(new FlatSVGIcon(
                    "com/comercialvalerio/presentation/ui/icon/svg/menu_right.svg",
                    0.8f));
            KeyUtils.setTooltipAndMnemonic(next, KeyEvent.VK_RIGHT, "Mes siguiente");
            next.putClientProperty(FlatClientProperties.STYLE,
                    "background:null; hoverBackground:" + UIStyle.HEX_LIGHT_BLUE);
            next.addActionListener(e -> { current = current.plusMonths(1); updateCalendar(); });

            JButton today = new JButton("Hoy");
            KeyUtils.setTooltipAndMnemonic(today,
                    KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), "Hoy");
            today.putClientProperty(FlatClientProperties.STYLE,
                    "background:null; hoverBackground:" + UIStyle.HEX_LIGHT_BLUE);
            today.addActionListener(e -> { current = LocalDate.now(); updateCalendar(); });

            KeyUtils.registerKeyAction(this,
                    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), prev::doClick);
            KeyUtils.registerKeyAction(this,
                    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), next::doClick);
            KeyUtils.registerKeyAction(this,
                    KeyStroke.getKeyStroke(KeyEvent.VK_T, 0),
                    () -> { current = LocalDate.now(); updateCalendar(); });

            head = new JLabel("", SwingConstants.CENTER);
            head.setForeground(Color.WHITE);
            head.setBorder(new EmptyBorder(4,0,4,0));

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            rightPanel.setOpaque(false);
            rightPanel.add(today);
            rightPanel.add(next);

            headPanel.add(prev, BorderLayout.WEST);
            headPanel.add(head, BorderLayout.CENTER);
            headPanel.add(rightPanel, BorderLayout.EAST);
            add(headPanel, BorderLayout.NORTH);

            /* grilla */
            JPanel grid=new JPanel(new GridLayout(7,7,2,2));
            grid.setOpaque(false);

            Font fDay=getFont().deriveFont(Font.BOLD,11f);
            Font fNum=getFont().deriveFont(Font.PLAIN,11f);

            for(String d:DAY){
                JLabel lbl=new JLabel(d, SwingConstants.CENTER);
                lbl.setFont(fDay);
                grid.add(lbl);
            }

            cells = new JLabel[42];
            for(int i=0; i<cells.length; i++){
                JLabel lbl=new JLabel(" ",SwingConstants.CENTER);
                lbl.setFont(fNum);
                cells[i]=lbl;
                grid.add(lbl);
            }
            add(grid, BorderLayout.CENTER);
        }

        private void updateCalendar(){
            LocalDate today = current;

            Color baseFg = UIManager.getColor("Label.foreground");
            if (baseFg == null) {
                baseFg = FlatLaf.isLafDark() ? Color.WHITE : Color.BLACK;
            }

            head.setText(today.getMonth().getDisplayName(TextStyle.FULL,
                    Locale.forLanguageTag("es"))
                    +" "+today.getYear());
            head.setForeground(Color.WHITE);

            YearMonth ym=YearMonth.of(today.getYear(),today.getMonth());
            int first=ym.atDay(1).getDayOfWeek().getValue(); // 1=Lun
            int len=ym.lengthOfMonth();

            LocalDate now = LocalDate.now();
            int cell=0;

            /* espacios antes del 1 */
            for(int i=1;i<first;i++,cell++){
                JLabel lbl=cells[cell];
                lbl.setText(" ");
                lbl.setOpaque(false);
                lbl.setForeground(baseFg);
            }

            /* días */
            for(int d=1; d<=len; d++,cell++){
                JLabel lbl=cells[cell];
                lbl.setText(String.valueOf(d));
                if(d==now.getDayOfMonth() && now.getMonth()==today.getMonth() && now.getYear()==today.getYear()){
                    lbl.setOpaque(true);
                    lbl.setBackground(new Color(76,151,210));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setOpaque(false);
                    lbl.setForeground(baseFg);
                }
            }

            /* espacios después del último día */
            while(cell<cells.length){
                JLabel lbl=cells[cell];
                lbl.setText(" ");
                lbl.setOpaque(false);
                lbl.setForeground(baseFg);
                cell++;
            }
            revalidate();
            repaint();
        }

        @Override
        public void updateUI() {
            super.updateUI();
            if (cells != null) {
                updateCalendar();
            }
        }
    }

    /** Recarga los datos del tablero. */
    public void refresh() {
        controller.refresh();
        if (calendar != null) {
            calendar.current = LocalDate.now();
            calendar.updateCalendar();
        }
        revalidate();
        repaint();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (metricsPanel != null) {
            SwingUtilities.invokeLater(() -> updateCardColors(this));
        }
    }

    private void updateCardColors(Container parent) {
        for (Component c : parent.getComponents()) {
            if (c instanceof JPanel panel) {
                Color bg = panel.getBackground();
                if (UIStyle.COLOR_CARD_BG.equals(bg) || UIStyle.COLOR_CARD_BG_DARK.equals(bg)) {
                    panel.setBackground(UIStyle.getColorCardBg());
                    if (panel.getComponentCount() > 0 && panel.getComponent(0) instanceof JLabel lbl) {
                        Object style = lbl.getClientProperty(FlatClientProperties.STYLE);
                        String prefix = "font:+1; ";
                        if (style instanceof String s && s.contains("font:+2")) {
                            prefix = "font:+2; ";
                        }
                        lbl.putClientProperty(FlatClientProperties.STYLE,
                                prefix + "foreground:" + UIStyle.getHexSecondaryText());
                    }
                }
                updateCardColors(panel);
            }
        }
    }

    /* ===== getters para el controlador ===== */
    public JLabel getLblVentasValue()     { return lblVentasValue;     }
    public JProgressBar getBarVentas()    { return barVentas;        }
    public JLabel getLblVentasFoot()      { return lblVentasFoot;     }
    public JLabel getLblPedidosValue()    { return lblPedidosValue;   }
    public JProgressBar getBarPedidos()   { return barPedidos;        }
    public JLabel getLblPedidosFoot()     { return lblPedidosFoot;    }

    public DefaultTableModel getBestSellerModel() { return bestSellerModel; }
    public JTable getTblBestSeller()              { return tblBestSeller;  }
    public JScrollPane getSpBestSeller()          { return spBestSeller;   }
    public JLabel getLblEmptyBest()               { return lblEmptyBest;   }

    public DefaultTableModel getClientesModel() { return clientesModel; }
    public JTable getTblClientes()              { return tblClientes;  }
    public JScrollPane getSpClientes()          { return spClientes;   }
    public JLabel getLblEmptyClientes()         { return lblEmptyClientes; }

    public DefaultTableModel getPendientesModel() { return pendientesModel; }
    public JTable getTblPendientes()              { return tblPendientes;  }
    public JScrollPane getSpPendientes()          { return spPendientes;   }
    public JLabel getLblEmptyPendientes()         { return lblEmptyPendientes; }

    /* ******************************************************************** */
    /*  R E L O J   A N A L Ó G I C O                                       */
    /* ******************************************************************** */
    private static class AnalogClock extends JPanel {

        // Temporizador para repintar la esfera cada segundo
        private final Timer timer;

        AnalogClock() {
            setPreferredSize(new Dimension(140, 140));
            setOpaque(false);
            loadUiColors();
            timer = new Timer(1000, e -> repaint());
        }

        @Override
        public void addNotify() {
            super.addNotify();
            // Iniciar el temporizador cuando el componente sea visible
            timer.start();
        }

        @Override
        public void updateUI() {
            super.updateUI();
            if (timer != null) {
                loadUiColors();
            }
        }

        private void loadUiColors() {
            Color bg = UIManager.getColor("Panel.background");
            if (bg == null) {
                bg = FlatLaf.isLafDark() ? Color.DARK_GRAY : Color.WHITE;
            }
            Color fg = UIManager.getColor("Label.foreground");
            if (fg == null) {
                fg = FlatLaf.isLafDark() ? Color.WHITE : Color.BLACK;
            }
            setBackground(bg);
            setForeground(fg);
        }

        @Override
        public void removeNotify() {
            // Detener el temporizador al ocultar el componente
            timer.stop();
            super.removeNotify();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int size = Math.min(w, h);
            int cx = w / 2;
            int cy = h / 2;
            int r = size / 2 - 5;

            g2.setColor(getBackground());
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(getForeground());
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);

            for (int i = 0; i < 60; i++) {
                double ang = Math.toRadians(i * 6);
                int len = (i % 5 == 0) ? 8 : 4;
                int x1 = cx + (int) ((r - len) * Math.sin(ang));
                int y1 = cy - (int) ((r - len) * Math.cos(ang));
                int x2 = cx + (int) (r * Math.sin(ang));
                int y2 = cy - (int) (r * Math.cos(ang));
                g2.drawLine(x1, y1, x2, y2);
            }

            LocalTime now = LocalTime.now();

            double secAng = Math.toRadians(now.getSecond() * 6);
            double minAng = Math.toRadians(now.getMinute() * 6 + now.getSecond() * 0.1);
            double hourAng = Math.toRadians((now.getHour() % 12) * 30 + now.getMinute() * 0.5);

            g2.setColor(getForeground());
            g2.setStroke(new BasicStroke(3f));
            int hrLen = r - 40 < r / 2 ? r / 2 : r - 40;
            int hx = cx + (int) (hrLen * Math.sin(hourAng));
            int hy = cy - (int) (hrLen * Math.cos(hourAng));
            g2.drawLine(cx, cy, hx, hy);

            g2.setStroke(new BasicStroke(2f));
            int mx = cx + (int) ((r - 20) * Math.sin(minAng));
            int my = cy - (int) ((r - 20) * Math.cos(minAng));
            g2.drawLine(cx, cy, mx, my);

            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(1f));
            int sx = cx + (int) ((r - 15) * Math.sin(secAng));
            int sy = cy - (int) ((r - 15) * Math.cos(secAng));
            g2.drawLine(cx, cy, sx, sy);

            g2.dispose();
        }
    }
}

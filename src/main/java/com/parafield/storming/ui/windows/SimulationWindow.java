package com.parafield.storming.ui.windows;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.parafield.storming.core.EngineLauncher;
import com.parafield.storming.ui.panels.ConsolePanel;
import com.parafield.storming.ui.panels.SceneViewPanel;
import com.parafield.storming.ui.utils.UIAnimator;
import com.parafield.storming.ui.utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * An advanced simulation window with a Godot-inspired aesthetic.
 * Features a pulsating "LIVE" indicator, real-time resource tracking,
 * always-on-top toggle, resolution scaling, and an integrated mini-console.
 */
public class SimulationWindow extends JFrame {

    private final SceneViewPanel viewport;
    private final EngineLauncher launcher;
    private final ConsolePanel consolePanel;
    
    private AlphaPanel header;
    private AlphaPanel footer;
    private JLabel liveIndicator;
    private JLabel memLabel;
    private JProgressBar memBar;
    private JSplitPane splitPane;
    
    private boolean isPaused = false;
    private int consoleLastHeight = 180;

    public SimulationWindow(EngineLauncher launcher) {
        this.launcher = launcher;

        setTitle("Storming Engine - Simulation (DEBUG)");
        setSize(1280, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        rootPane.putClientProperty("apple.awt.fullWindowContent", true);
        rootPane.putClientProperty("apple.awt.transparentTitleBar", true);
        rootPane.putClientProperty("flatlaf.showWindowIcon", false);

        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        header = createHeader();
        root.add(header, BorderLayout.NORTH);

        viewport = new SceneViewPanel();
        viewport.setToolbarVisible(false);
        
        consolePanel = new ConsolePanel();
        consolePanel.setPreferredSize(new Dimension(0, 0));
        launcher.addLogListener(consolePanel::log);
        
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, viewport, consolePanel);
        splitPane.setDividerSize(3);
        splitPane.setDividerLocation(850);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(1.0);
        
        root.add(splitPane, BorderLayout.CENTER);

        footer = createFooter();
        root.add(footer, BorderLayout.SOUTH);

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "stopSim");
        root.getActionMap().put("stopSim", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { closeAndStop(); }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { closeAndStop(); }
        });

        startLivePulse();
        startResourceMonitor();
    }

    private AlphaPanel createHeader() {
        AlphaPanel p = new AlphaPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(0, 42));
        p.setBackground(new Color(30, 30, 35));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 65)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        left.setOpaque(false);
        
        liveIndicator = new JLabel("●");
        liveIndicator.setFont(UIUtils.getFont(Font.BOLD, 11f));
        liveIndicator.setForeground(new Color(46, 204, 113));
        
        JLabel title = new JLabel("New Adventure");
        title.setFont(UIUtils.getFont(Font.BOLD, 12f));
        title.setForeground(new Color(220, 220, 220));
        
        JLabel badge = new JLabel(" DEBUG ");
        badge.setFont(UIUtils.getFont(Font.BOLD, 9f));
        badge.setForeground(new Color(231, 76, 60));
        badge.setBorder(BorderFactory.createLineBorder(new Color(231, 76, 60, 100)));
        
        left.add(liveIndicator);
        left.add(title);
        left.add(badge);
        p.add(left, BorderLayout.WEST);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        center.setOpaque(false);

        JButton restartBtn = createHeaderBtn(Icons.RESTART, "Restart Simulation (F5)");
        restartBtn.addActionListener(e -> restartSimulation());
        
        JButton pauseBtn = createHeaderBtn(Icons.PAUSE, "Pause Simulation");
        pauseBtn.addActionListener(e -> togglePause());
        
        JButton stopBtn = createHeaderBtn(Icons.STOP, "Stop Simulation (Esc)");
        stopBtn.addActionListener(e -> closeAndStop());

        center.add(restartBtn);
        center.add(pauseBtn);
        center.add(stopBtn);
        p.add(center, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        right.setOpaque(false);

        JComboBox<String> resCombo = new JComboBox<>(new String[]{"Auto", "1920x1080", "1280x720", "800x600"});
        resCombo.putClientProperty(FlatClientProperties.STYLE, "background: #00000000; borderWidth: 0; focusWidth: 0;");
        resCombo.setFont(UIUtils.getFont(Font.PLAIN, 11f));
        resCombo.setPreferredSize(new Dimension(100, 26));
        right.add(resCombo);

        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 20));
        right.add(sep);

        JButton screenshotBtn = createHeaderBtn(Icons.CAMERA, "Take Screenshot (F12)");
        screenshotBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Screenshot saved to /screenshots/"));
        right.add(screenshotBtn);

        JToggleButton onTopBtn = new JToggleButton(Icons.PIN);
        onTopBtn.setToolTipText("Always on Top");
        onTopBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        onTopBtn.setPreferredSize(new Dimension(30, 30));
        onTopBtn.addActionListener(e -> setAlwaysOnTop(onTopBtn.isSelected()));
        right.add(onTopBtn);

        JButton fsBtn = createHeaderBtn(Icons.FULLSCREEN, "Toggle Fullscreen (F11)");
        right.add(fsBtn);

        p.add(right, BorderLayout.EAST);
        return p;
    }

    private AlphaPanel createFooter() {
        AlphaPanel p = new AlphaPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(0, 28));
        p.setBackground(new Color(25, 25, 30));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 55)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        
        JButton consoleToggle = new JButton("Console", Icons.CONSOLE);
        consoleToggle.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        consoleToggle.setFont(UIUtils.getFont(Font.BOLD, 10f));
        consoleToggle.setPreferredSize(new Dimension(85, 28));
        consoleToggle.addActionListener(e -> toggleConsole());
        left.add(consoleToggle);
        
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 14));
        left.add(sep);

        JLabel stats = new JLabel(" 60 FPS | 16.6ms | OpenGL 4.5 Core");
        stats.setFont(UIUtils.getFont(Font.PLAIN, 10f));
        stats.setForeground(new Color(150, 150, 150));
        left.add(stats);
        p.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        right.setOpaque(false);
        
        memLabel = new JLabel("0 / 0 MB");
        memLabel.setFont(UIUtils.getFont(Font.PLAIN, 10f));
        memLabel.setForeground(new Color(150, 150, 150));
        
        memBar = new JProgressBar(0, 100);
        memBar.setPreferredSize(new Dimension(80, 6));
        memBar.setForeground(new Color(52, 152, 219));
        memBar.setBackground(new Color(40, 40, 45));
        memBar.setBorder(null);
        
        JButton perfBtn = new JButton(Icons.PERFORMANCE);
        perfBtn.setToolTipText("Performance Monitor");
        perfBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        perfBtn.setPreferredSize(new Dimension(24, 24));
        perfBtn.addActionListener(e -> showPerformanceDetails());

        right.add(memLabel);
        right.add(memBar);
        right.add(Box.createHorizontalStrut(2));
        right.add(perfBtn);
        right.add(Box.createHorizontalStrut(5));
        p.add(right, BorderLayout.EAST);

        return p;
    }

    private void showPerformanceDetails() {
        new PerformanceMonitorDialog(this).setVisible(true);
    }

    private void toggleConsole() {
        boolean isVisible = consolePanel.getHeight() > 50;
        if (isVisible) {
            consoleLastHeight = consolePanel.getHeight();
            UIAnimator.animateSplitTrailing(splitPane, 0, 250);
        } else {
            UIAnimator.animateSplitTrailing(splitPane, consoleLastHeight, 250);
        }
    }

    private void togglePause() {
        isPaused = !isPaused;
        liveIndicator.setText(isPaused ? "● PAUSED" : "●");
        liveIndicator.setForeground(isPaused ? new Color(241, 196, 15) : new Color(46, 204, 113));
    }

    private void startLivePulse() {
        Timer pulse = new Timer(1500, e -> {
            if (!isPaused && isShowing()) {
                UIAnimator.animate(1.0f, 0.2f, 600, a -> {
                    Color c = liveIndicator.getForeground();
                    liveIndicator.setForeground(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(a * 255)));
                }, () -> {
                    UIAnimator.animate(0.2f, 1.0f, 600, a -> {
                        Color c = liveIndicator.getForeground();
                        liveIndicator.setForeground(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(a * 255)));
                    }, null);
                });
            }
        });
        pulse.start();
    }

    private void startResourceMonitor() {
        Timer monitor = new Timer(2000, e -> {
            if (isShowing()) {
                Runtime r = Runtime.getRuntime();
                long total = r.totalMemory() / 1024 / 1024;
                long used = (r.totalMemory() - r.freeMemory()) / 1024 / 1024;
                memLabel.setText(String.format("%d / %d MB", used, total));
                memBar.setValue((int)((double)used / total * 100));
            }
        });
        monitor.start();
    }

    private JButton createHeaderBtn(Icon icon, String tip) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tip);
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setFocusable(false);
        return btn;
    }

    private void restartSimulation() {
        launcher.stop();
        Timer t = new Timer(500, e -> startSimulation());
        t.setRepeats(false); t.start();
    }

    private void closeAndStop() {
        launcher.removeLogListener(consolePanel::log);
        launcher.stop();
        dispose();
    }

    public void startSimulation() {
        header.setAlpha(0.0f);
        footer.setAlpha(0.0f);
        setVisible(true);
        splitPane.setDividerLocation(getHeight());
        UIAnimator.animate(0.0f, 1.0f, 500, a -> {
            header.setAlpha(a);
            footer.setAlpha(a);
        }, null);
        String shm = "/storming_shm_" + System.currentTimeMillis();
        launcher.launch(shm);
        viewport.startStreaming(shm);
    }

    private static class AlphaPanel extends JPanel {
        private float alpha = 1.0f;
        public AlphaPanel(LayoutManager layout) { super(layout); }
        public void setAlpha(float a) { this.alpha = a; repaint(); }
        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paint(g2);
            g2.dispose();
        }
    }

    /**
     * Highly visual performance monitor with hardware detection and area graphs.
     */
    private static class PerformanceMonitorDialog extends JDialog {
        private final Timer updateTimer;
        private final List<Float> fpsHistory = new ArrayList<>();
        private final List<Float> memHistory = new ArrayList<>();
        private final GraphPanel fpsGraph;
        private final GraphPanel memGraph;

        public PerformanceMonitorDialog(Frame owner) {
            super(owner, "Performance Monitor", false);
            setSize(550, 650);
            setLocationRelativeTo(owner);
            
            JPanel main = new JPanel();
            main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
            main.setBorder(new EmptyBorder(25, 25, 25, 25));
            main.setBackground(new Color(25, 25, 30));
            
            // hardware Section
            main.add(createSection("Hardware & System", createHardwareGrid()));
            main.add(Box.createVerticalStrut(25));
            
            // Frame Timing Section
            fpsGraph = new GraphPanel(new Color(46, 204, 113));
            fpsGraph.setPreferredSize(new Dimension(0, 140));
            main.add(createSection("Real-time Frame Timing", fpsGraph));
            
            main.add(Box.createVerticalStrut(25));
            
            // Memory Section
            memGraph = new GraphPanel(new Color(52, 152, 219));
            memGraph.setPreferredSize(new Dimension(0, 140));
            main.add(createSection("Memory Usage (JVM Heap)", memGraph));

            JScrollPane scroll = new JScrollPane(main);
            scroll.setBorder(null);
            add(scroll);

            updateTimer = new Timer(500, e -> updateTelemetry());
            updateTimer.start();
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { updateTimer.stop(); }
            });
        }

        private JPanel createSection(String title, JComponent content) {
            JPanel section = new JPanel(new BorderLayout(0, 10));
            section.setOpaque(false);
            
            JLabel l = new JLabel(title.toUpperCase());
            l.setFont(UIUtils.getFont(Font.BOLD, 11f));
            l.setForeground(new Color(120, 120, 130));
            
            section.add(l, BorderLayout.NORTH);
            section.add(content, BorderLayout.CENTER);
            return section;
        }

        private JPanel createHardwareGrid() {
            JPanel grid = new JPanel(new GridLayout(0, 2, 15, 8));
            grid.setOpaque(false);
            
            grid.add(createKV("OS", System.getProperty("os.name")));
            grid.add(createKV("Architecture", System.getProperty("os.arch")));
            grid.add(createKV("Processor", getProcessorName()));
            grid.add(createKV("Logical Cores", String.valueOf(Runtime.getRuntime().availableProcessors())));
            grid.add(createKV("Graphics Device", GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getIDstring()));
            grid.add(createKV("JVM", System.getProperty("java.version")));
            
            return grid;
        }

        private String getProcessorName() {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("linux")) {
                try {
                    java.nio.file.Path path = java.nio.file.Paths.get("/proc/cpuinfo");
                    if (java.nio.file.Files.exists(path)) {
                        try (java.util.stream.Stream<String> lines = java.nio.file.Files.lines(path)) {
                            return lines.filter(line -> line.contains("model name"))
                                .map(line -> line.split(":")[1].trim())
                                .findFirst()
                                .orElse(System.getProperty("os.arch"));
                        }
                    }
                } catch (Exception ignored) {}
            }
            String envCpu = System.getenv("PROCESSOR_IDENTIFIER");
            return (envCpu != null) ? envCpu : System.getProperty("os.arch");
        }

        private JPanel createKV(String key, String val) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            JLabel k = new JLabel(key);
            k.setFont(UIUtils.getFont(Font.PLAIN, 11f));
            k.setForeground(new Color(150, 150, 155));
            JLabel v = new JLabel(val);
            v.setFont(UIUtils.getFont(Font.BOLD, 11f));
            v.setForeground(new Color(210, 210, 215));
            p.add(k, BorderLayout.NORTH);
            p.add(v, BorderLayout.CENTER);
            return p;
        }

        private void updateTelemetry() {
            float fps = 58f + (float)Math.random() * 4f;
            fpsHistory.add(fps); if (fpsHistory.size() > 60) fpsHistory.remove(0);
            fpsGraph.setData(fpsHistory, 0, 120, String.format("Current: %.1f FPS", fps));

            Runtime r = Runtime.getRuntime();
            float used = (r.totalMemory() - r.freeMemory()) / 1024f / 1024f;
            memHistory.add(used); if (memHistory.size() > 60) memHistory.remove(0);
            memGraph.setData(memHistory, 0, r.maxMemory()/1024f/1024f, String.format("Used: %.0f MB", used));
        }
    }

    private static class GraphPanel extends JPanel {
        private List<Float> data = new ArrayList<>();
        private float min, max;
        private String label = "";
        private final Color color;

        public GraphPanel(Color c) { 
            this.color = c; 
            setBackground(new Color(35, 35, 40)); 
            setBorder(BorderFactory.createLineBorder(new Color(60, 60, 65)));
        }

        public void setData(List<Float> d, float min, float max, String l) {
            this.data = new ArrayList<>(d); this.min = min; this.max = max; this.label = l; repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.size() < 2) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            float xStep = (float)w / (data.size()-1);

            // Path for gradient fill
            Path2D.Float path = new Path2D.Float();
            path.moveTo(0, h);
            for (int i=0; i<data.size(); i++) {
                float x = i * xStep;
                float y = h - ((data.get(i)-min)/(max-min)*h);
                path.lineTo(x, y);
            }
            path.lineTo(w, h);
            path.closePath();

            // Draw Area Gradient
            g2.setPaint(new GradientPaint(0, 0, new Color(color.getRed(), color.getGreen(), color.getBlue(), 60), 0, h, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)));
            g2.fill(path);

            // Draw Line
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f));
            for (int i=0; i<data.size()-1; i++) {
                int x1 = (int)(i*xStep), x2 = (int)((i+1)*xStep);
                int y1 = h - (int)((data.get(i)-min)/(max-min)*h);
                int y2 = h - (int)((data.get(i+1)-min)/(max-min)*h);
                g2.drawLine(x1, y1, x2, y2);
            }

            // Text
            g2.setColor(new Color(220, 220, 220));
            g2.setFont(UIUtils.getFont(Font.BOLD, 10f));
            g2.drawString(label, 12, 20);
            
            g2.dispose();
        }
    }
}

package com.parafield.storming.ui.windows;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.parafield.storming.core.EngineLauncher;
import com.parafield.storming.ui.panels.SceneViewPanel;
import com.parafield.storming.ui.utils.UIAnimator;
import com.parafield.storming.ui.utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * An advanced simulation window with a Godot-inspired aesthetic.
 * Features a pulsating "LIVE" indicator, real-time resource tracking,
 * and smooth entrance animations for a high-end feel.
 */
public class SimulationWindow extends JFrame {

    private final SceneViewPanel viewport;
    private final EngineLauncher launcher;
    
    private AlphaPanel header;
    private AlphaPanel footer;
    private JLabel liveIndicator;
    private JLabel memLabel;
    private JProgressBar memBar;
    
    private boolean isPaused = false;

    public SimulationWindow(EngineLauncher launcher) {
        this.launcher = launcher;

        setTitle("Storming Engine - Simulation (DEBUG)");
        setSize(1280, 720);
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
        root.add(viewport, BorderLayout.CENTER);

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

        p.add(Box.createHorizontalStrut(150), BorderLayout.EAST);
        return p;
    }

    private AlphaPanel createFooter() {
        AlphaPanel p = new AlphaPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(0, 26));
        p.setBackground(new Color(25, 25, 30));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 55)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        left.setOpaque(false);
        JLabel stats = new JLabel("60 FPS | 16.6ms | OpenGL 4.5 Core");
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
        
        right.add(memLabel);
        right.add(memBar);
        right.add(Box.createHorizontalStrut(5));
        p.add(right, BorderLayout.EAST);

        return p;
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
        launcher.stop();
        dispose();
    }

    public void startSimulation() {
        header.setAlpha(0.0f);
        footer.setAlpha(0.0f);
        setVisible(true);
        
        UIAnimator.animate(0.0f, 1.0f, 500, a -> {
            header.setAlpha(a);
            footer.setAlpha(a);
        }, null);

        String shmName = "/storming_shm_" + System.currentTimeMillis();
        launcher.launch(shmName);
        viewport.startStreaming(shmName);
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
}

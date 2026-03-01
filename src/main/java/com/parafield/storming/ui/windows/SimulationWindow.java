package com.parafield.storming.ui.windows;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.parafield.storming.core.EngineLauncher;
import com.parafield.storming.ui.panels.SceneViewPanel;
import com.parafield.storming.ui.utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A simulation window inspired by Godot's runtime environment.
 * Features a streamlined top bar with centered controls and a minimal footer
 * for a focused debugging experience.
 */
public class SimulationWindow extends JFrame {

    private final SceneViewPanel viewport;
    private final EngineLauncher launcher;
    
    private JLabel statusLabel;
    private JLabel fpsLabel;

    /**
     * Constructs a SimulationWindow with a Godot-inspired layout.
     * @param launcher The EngineLauncher instance to manage the engine process.
     */
    public SimulationWindow(EngineLauncher launcher) {
        this.launcher = launcher;

        setTitle("Storming Engine - Simulation (DEBUG)");
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Modern Window setup
        rootPane.putClientProperty("apple.awt.fullWindowContent", true);
        rootPane.putClientProperty("apple.awt.transparentTitleBar", true);
        rootPane.putClientProperty("flatlaf.showWindowIcon", false);

        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // --- 1. Godot-style Header ---
        root.add(createHeader(), BorderLayout.NORTH);

        // --- 2. Main Viewport ---
        viewport = new SceneViewPanel();
        root.add(viewport, BorderLayout.CENTER);

        // --- 3. Minimal Footer ---
        root.add(createFooter(), BorderLayout.SOUTH);

        // ESC to stop
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "stopSim");
        root.getActionMap().put("stopSim", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { closeAndStop(); }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { closeAndStop(); }
        });
    }

    /**
     * Creates the top bar with project info and centered playback controls.
     * @return A JPanel configured as a header.
     */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 42));
        header.setBackground(new Color(30, 30, 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 65)));

        // Left: Title & Debug Badge
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        leftPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("New Adventure");
        titleLabel.setFont(UIUtils.getFont(Font.BOLD, 12f));
        titleLabel.setForeground(new Color(220, 220, 220));
        
        JLabel debugBadge = new JLabel(" DEBUG ");
        debugBadge.setFont(UIUtils.getFont(Font.BOLD, 10f));
        debugBadge.setForeground(new Color(231, 76, 60));
        debugBadge.setBorder(BorderFactory.createLineBorder(new Color(231, 76, 60, 150), 1));
        
        leftPanel.add(titleLabel);
        leftPanel.add(debugBadge);
        header.add(leftPanel, BorderLayout.WEST);

        // Center: Playback Controls
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 4));
        centerPanel.setOpaque(false);

        JButton restartBtn = createHeaderBtn(Icons.RESTART, "Restart Simulation (F5)");
        restartBtn.addActionListener(e -> restartSimulation());
        
        JButton pauseBtn = createHeaderBtn(Icons.PAUSE, "Pause Simulation");
        
        JButton stopBtn = createHeaderBtn(Icons.STOP, "Stop Simulation (Esc)");
        stopBtn.addActionListener(e -> closeAndStop());

        centerPanel.add(restartBtn);
        centerPanel.add(pauseBtn);
        centerPanel.add(stopBtn);
        header.add(centerPanel, BorderLayout.CENTER);

        // Right: Placeholder for search or filter
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        rightPanel.setOpaque(false);
        rightPanel.add(Box.createHorizontalStrut(150)); // Match mac window buttons space
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    /**
     * Creates the status bar footer.
     * @return A JPanel configured as a footer.
     */
    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setPreferredSize(new Dimension(0, 24));
        footer.setBackground(new Color(25, 25, 30));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 55)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        left.setOpaque(false);
        statusLabel = new JLabel("● Running Storming Engine 2D");
        statusLabel.setFont(UIUtils.getFont(Font.PLAIN, 11f));
        statusLabel.setForeground(new Color(46, 204, 113));
        left.add(statusLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 2));
        right.setOpaque(false);
        fpsLabel = new JLabel("60 FPS | 16.6ms | OpenGL 4.5 Core");
        fpsLabel.setFont(UIUtils.getFont(Font.PLAIN, 11f));
        fpsLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        right.add(fpsLabel);

        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private JButton createHeaderBtn(Icon icon, String tip) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tip);
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btn.setPreferredSize(new Dimension(34, 34));
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

    /**
     * Initiates the simulation process and starts the shared memory stream.
     */
    public void startSimulation() {
        setVisible(true);
        String shmName = "/storming_shm_" + System.currentTimeMillis();
        launcher.launch(shmName);
        viewport.startStreaming(shmName);
    }
}

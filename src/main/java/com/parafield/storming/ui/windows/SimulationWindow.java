package com.parafield.storming.ui.windows;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.parafield.storming.core.EngineLauncher;
import com.parafield.storming.ui.panels.SceneViewPanel;
import com.parafield.storming.ui.utils.UIUtils;
import javax.swing.*;
import java.awt.*;

/**
 * A dedicated window for running and debugging engine simulations.
 * Provides a clean viewport for the game/application and playback controls.
 * Integrates with the {@link EngineLauncher} to manage the engine process.
 */
public class SimulationWindow extends JFrame {

    private final SceneViewPanel viewport;
    private final EngineLauncher launcher;

    /**
     * Constructs a SimulationWindow.
     * @param launcher The EngineLauncher instance to use for starting/stopping the engine.
     */
    public SimulationWindow(EngineLauncher launcher) {
        this.launcher = launcher;

        setTitle("New Adventure (DEBUG) - Storming Engine");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Integrated Title Bar setup
        rootPane.putClientProperty("apple.awt.fullWindowContent", true);
        rootPane.putClientProperty("apple.awt.transparentTitleBar", true);
        rootPane.putClientProperty("flatlaf.showWindowIcon", false);

        initMenuBar();

        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // 1. The Viewport (Godot style: pure game focus)
        viewport = new SceneViewPanel();
        root.add(viewport, BorderLayout.CENTER);

        // 2. Minimal Footer (Optional, can be removed for full clean look)
        JPanel footer = new JPanel(new BorderLayout());
        footer.setPreferredSize(new Dimension(0, 22));
        footer.setBackground(new Color(20, 20, 25));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(100, 100, 100, 30)));
        
        JLabel stats = new JLabel("  ● Live | 60 FPS | OpenGL 4.5 Core");
        stats.setFont(UIUtils.getFont(Font.PLAIN, 10f));
        stats.setForeground(new Color(150, 150, 150));
        footer.add(stats, BorderLayout.WEST);
        
        root.add(footer, BorderLayout.SOUTH);

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
     * Initializes the menu bar with simulation playback controls (Restart, Pause, Stop).
     */
    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setPreferredSize(new Dimension(0, 40));
        menuBar.setBackground(new Color(25, 25, 30));
        
        // Left: Title
        JLabel titleLabel = new JLabel("  New Adventure (DEBUG)");
        titleLabel.setFont(UIUtils.getFont(Font.BOLD, 12f));
        titleLabel.setForeground(new Color(200, 200, 200));
        menuBar.add(titleLabel);

        menuBar.add(Box.createHorizontalGlue());

        // Center: Godot-style Playback Controls
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        centerPanel.setOpaque(false);

        JButton restartBtn = createControlBtn(Icons.RESTART, "Restart (F5)");
        restartBtn.addActionListener(e -> restartSimulation());
        
        JButton pauseBtn = createControlBtn(Icons.PAUSE, "Pause");
        
        JButton stopBtn = createControlBtn(Icons.STOP, "Stop (Esc)");
        stopBtn.addActionListener(e -> closeAndStop());

        centerPanel.add(restartBtn);
        centerPanel.add(pauseBtn);
        centerPanel.add(stopBtn);
        
        menuBar.add(centerPanel);
        menuBar.add(Box.createHorizontalGlue());
        
        // Right: Window controls space
        menuBar.add(Box.createHorizontalStrut(120));

        setJMenuBar(menuBar);
    }

    /**
     * Creates a stylized toolbar button for simulation control.
     * @param icon The icon to display on the button.
     * @param tip The tooltip text for the button.
     * @return A configured JButton.
     */
    private JButton createControlBtn(Icon icon, String tip) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tip);
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btn.setPreferredSize(new Dimension(34, 34));
        return btn;
    }

    /**
     * Restarts the engine simulation by stopping the current process and launching a new one.
     */
    private void restartSimulation() {
        launcher.stop();
        Timer t = new Timer(500, e -> startSimulation());
        t.setRepeats(false); t.start();
    }

    /**
     * Stops the simulation and closes the window.
     */
    private void closeAndStop() {
        launcher.stop();
        dispose();
    }

    /**
     * Starts the engine simulation.
     * Makes the window visible and initiates engine launch with shared memory.
     */
    public void startSimulation() {
        setVisible(true);
        String shmName = "/storming_shm_" + System.currentTimeMillis();
        launcher.launch(shmName);
        viewport.startStreaming(shmName);
    }
}

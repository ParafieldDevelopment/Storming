package com.parafield.storming.ui.windows;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.parafield.storming.core.EngineLauncher;
import com.parafield.storming.ui.panels.SceneViewPanel;
import javax.swing.*;
import java.awt.*;

public class SimulationWindow extends JFrame {

    private final SceneViewPanel viewport;
    private final EngineLauncher launcher;

    public SimulationWindow(EngineLauncher launcher) {
        this.launcher = launcher;
        
        setTitle("Storming Engine | Simulation");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Modern Window setup
        rootPane.putClientProperty("apple.awt.fullWindowContent", true);
        rootPane.putClientProperty("apple.awt.transparentTitleBar", true);

        JPanel mainContent = new JPanel(new BorderLayout());
        setContentPane(mainContent);

        // 1. Header Bar (Godot Style)
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 45));
        header.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 7%)");
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        leftGroup.setOpaque(false);
        JLabel titleLabel = new JLabel("RUNNING SIMULATION");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 12));
        titleLabel.setForeground(new Color(52, 152, 219)); // Blue
        leftGroup.add(titleLabel);
        
        JPanel centerGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
        centerGroup.setOpaque(false);
        
        JButton stopBtn = new JButton(new FlatSVGIcon("com/parafield/storming/icons/stop.svg", 18, 18));
        stopBtn.setToolTipText("Stop Simulation (Esc)");
        stopBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        stopBtn.addActionListener(e -> closeAndStop());
        centerGroup.add(stopBtn);

        header.add(leftGroup, BorderLayout.WEST);
        header.add(centerGroup, BorderLayout.CENTER);
        
        mainContent.add(header, BorderLayout.NORTH);

        // 2. The Viewport
        viewport = new SceneViewPanel();
        mainContent.add(viewport, BorderLayout.CENTER);

        // 3. Footer / Stats
        JPanel footer = new JPanel(new BorderLayout());
        footer.setPreferredSize(new Dimension(0, 25));
        footer.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 5%)");
        JLabel stats = new JLabel("  Vulkan 1.3 | Performance: High | Memory: 142MB");
        stats.setFont(new Font("Inter", Font.PLAIN, 11));
        stats.setForeground(UIManager.getColor("Label.disabledForeground"));
        footer.add(stats, BorderLayout.WEST);
        
        mainContent.add(footer, BorderLayout.SOUTH);

        // Keyboard Shortcut: ESC to stop
        mainContent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "stopSim");
        mainContent.getActionMap().put("stopSim", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                closeAndStop();
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeAndStop();
            }
        });
    }

    private void closeAndStop() {
        launcher.stop();
        this.dispose();
    }

    public void startSimulation() {
        setVisible(true);
        // We use a timer to ensure the Canvas is definitely mapped and has an X11 ID
        Timer timer = new Timer(500, e -> {
            long id = 0;
            for (int i = 0; i < 5 && id == 0; i++) {
                id = viewport.getNativeWindowID();
                if (id == 0) {
                    try { Thread.sleep(100); } catch (Exception ex) {}
                }
            }

            if (id != 0) {
                launcher.launch(id);
            } else {
                System.err.println("[System] Failed to retrieve Native Window ID. Falling back to standalone.");
                launcher.launch(0);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
}

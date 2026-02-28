package com.parafield.storming.ui.widgets;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import java.awt.*;

public class MainToolbar extends JToolBar {

    public MainToolbar(Runnable onPlay, Runnable onStop) {
        setFloatable(false);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
        setPreferredSize(new Dimension(0, 42));
        
        // --- 1. LEFT: Logo, Hamburger & Project Info ---
        add(Box.createHorizontalStrut(10));
        
        // App Logo first
        JLabel appLogo = new JLabel(Icons.EXE_ICON);
        add(appLogo);
        
        add(Box.createHorizontalStrut(5));
        
        // Hamburger second
        JButton menuBtn = new JButton(Icons.HAMBURGER);
        menuBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        menuBtn.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            menu.add(new JMenuItem("New Project"));
            menu.add(new JMenuItem("Open Project..."));
            menu.addSeparator();
            menu.add(new JMenuItem("Save Scene"));
            menu.show(menuBtn, 0, menuBtn.getHeight());
        });
        add(menuBtn);
        
        add(Box.createHorizontalStrut(12));
        
        // Project Icon (Letter in rounded square)
        String projectName = "New Adventure";
        JLabel projectIcon = new JLabel(projectName.substring(0, 1).toUpperCase());
        projectIcon.setOpaque(true);
        projectIcon.setBackground(new Color(52, 152, 219)); // Storm Blue
        projectIcon.setForeground(Color.WHITE);
        projectIcon.setFont(new Font("Inter", Font.BOLD, 11));
        projectIcon.setHorizontalAlignment(SwingConstants.CENTER);
        
        Dimension iconSize = new Dimension(20, 20);
        projectIcon.setPreferredSize(iconSize);
        projectIcon.setMinimumSize(iconSize);
        projectIcon.setMaximumSize(iconSize);
        projectIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 5");
        add(projectIcon);
        
        add(Box.createHorizontalStrut(6));
        JLabel nameLabel = new JLabel(projectName);
        nameLabel.setFont(new Font("Inter", Font.BOLD, 12));
        add(nameLabel);

        add(Box.createHorizontalStrut(15));

        // --- Git Branch Button ---
        JButton branchBtn = new JButton("master", Icons.GIT);
        branchBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        branchBtn.setFont(new Font("Inter", Font.PLAIN, 11));
        branchBtn.setForeground(UIManager.getColor("Label.disabledForeground"));
        branchBtn.addActionListener(e -> {
            JPopupMenu gitMenu = new JPopupMenu();

            gitMenu.add(new JMenuItem("Commit..."));
            gitMenu.add(new JMenuItem("Push"));
            gitMenu.add(new JMenuItem("Pull"));
            gitMenu.addSeparator();

            JMenu branchesSub = new JMenu("Branches");
            branchesSub.add(new JRadioButtonMenuItem("master", true));
            branchesSub.add(new JRadioButtonMenuItem("develop"));
            branchesSub.addSeparator();
            branchesSub.add(new JMenuItem("New Branch..."));
            gitMenu.add(branchesSub);

            gitMenu.addSeparator();
            gitMenu.add(new JMenuItem("Git Log"));

            gitMenu.show(branchBtn, 0, branchBtn.getHeight());
        });
        add(branchBtn);
        
        add(Box.createHorizontalGlue());
        
        // --- 2. CENTER: Workspace Selectors (Hidden for now) ---
        /*
        JPanel workspacePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 5));
        workspacePanel.setOpaque(false);
        String[] workspaces = {"2D Scene", "3D Scene", "Script", "AssetLib"};
        for (String ws : workspaces) {
            JToggleButton btn = new JToggleButton(ws);
            btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
            workspacePanel.add(btn);
        }
        add(workspacePanel);
        add(Box.createHorizontalGlue());
        */

        // --- 3. RIGHT: Run Controls & Settings ---
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        controls.setOpaque(false);
        
        JButton playBtn = new JButton(Icons.PLAY);
        playBtn.setToolTipText("Run (F5)");
        playBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        playBtn.addActionListener(e -> onPlay.run());
        
        JButton stopBtn = new JButton(Icons.STOP);
        stopBtn.setToolTipText("Stop (Shift+F5)");
        stopBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        stopBtn.addActionListener(e -> onStop.run());

        JButton settingsBtn = new JButton(Icons.SETTINGS);
        settingsBtn.setToolTipText("Settings");
        settingsBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);

        controls.add(playBtn);
        controls.add(stopBtn);
        controls.add(Box.createHorizontalStrut(5));
        controls.add(settingsBtn);
        
        add(controls);
        add(Box.createHorizontalStrut(10));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calculate center for the glow (around project icon/name)
        int centerX = 160; 
        int centerY = getHeight() / 2;
        
        // Spread it more horizontally
        int radiusX = 350;
        int radiusY = getHeight() * 2;

        float[] dist = {0.0f, 0.3f, 1.0f};
        // Increased alpha (100) for the core, fading out
        Color glowColor = new Color(52, 152, 219); 
        Color[] colors = {
            new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 110),
            new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 45),
            new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 0)
        };
        
        RadialGradientPaint p = new RadialGradientPaint(centerX, centerY, radiusX, dist, colors);
        g2.setPaint(p);
        
        // Use a wide oval to spread the glow along the toolbar
        g2.fillOval(centerX - radiusX, centerY - radiusY, radiusX * 2, radiusY * 2);

        g2.dispose();
    }
}

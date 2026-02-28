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
}

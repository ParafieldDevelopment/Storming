package com.parafield.storming.ui.widgets;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import java.awt.*;

public class MainToolbar extends JToolBar {
    private float alpha = 1.0f;

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        super.paint(g2);
        g2.dispose();
    }

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

        // --- Project Dropdown Button ---
        String projectName = "New Adventure";
        JButton projectBtn = new JButton(projectName, new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(52, 152, 219));
                g2.fillRoundRect(x, y, 18, 18, 6, 6);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Inter", Font.BOLD, 11));
                String letter = projectName.substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(letter, x + (18 - fm.stringWidth(letter)) / 2, y + ((18 - fm.getHeight()) / 2) + fm.getAscent());
                g2.dispose();
            }
            @Override public int getIconWidth() { return 18; }
            @Override public int getIconHeight() { return 18; }
        });
        
        projectBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        projectBtn.putClientProperty(FlatClientProperties.STYLE, "margin: 0,10,0,10; arc: 8");
        projectBtn.setFont(new Font("Inter", Font.BOLD, 12));
        projectBtn.addActionListener(e -> {
            JPopupMenu projectMenu = new JPopupMenu();
            projectMenu.add(new JMenuItem("New Project..."));
            projectMenu.add(new JMenuItem("Open Project..."));
            projectMenu.addSeparator();
            
            JMenu recentMenu = new JMenu("Recent Projects");
            recentMenu.add(new JMenuItem("Storming Demo"));
            recentMenu.add(new JMenuItem("Old Project X"));
            projectMenu.add(recentMenu);
            
            projectMenu.show(projectBtn, 0, projectBtn.getHeight());
        });
        add(projectBtn);
        
        add(Box.createHorizontalStrut(8));
        
        // --- Git Branch Button ---
        JButton branchBtn = new JButton("master", Icons.GIT);
        branchBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        branchBtn.putClientProperty(FlatClientProperties.STYLE, "margin: 0,10,0,10; arc: 8");
        branchBtn.setFont(new Font("Inter", Font.BOLD, 12));
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
        int centerX = 220; 
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

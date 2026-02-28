package com.parafield.storming.ui.widgets;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import java.awt.*;

public class StormingMenuBar extends JMenuBar {
    private float alpha = 1.0f;
    private final Runnable onPlay;
    private final Runnable onStop;

    public StormingMenuBar(Runnable onPlay, Runnable onStop) {
        this.onPlay = onPlay;
        this.onStop = onStop;
        
        setPreferredSize(new Dimension(0, 40));
        putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #ffffff10;");
        
        initMenuBar();
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        repaint();
    }

    private void initMenuBar() {
        add(Box.createHorizontalStrut(10));
        
        // App Icon
        JLabel appLogo = new JLabel(Icons.EXE_ICON);
        add(appLogo);
        add(Box.createHorizontalStrut(10));

        // Menus
        add(createMenu("File"));
        add(createMenu("Edit"));
        add(createMenu("View"));
        
        add(Box.createHorizontalStrut(20));

        // Project Info (Center-ish)
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
        projectBtn.setFont(new Font("Inter", Font.BOLD, 12));
        add(projectBtn);
        
        add(Box.createHorizontalStrut(10));
        
        JButton branchBtn = new JButton("master", Icons.GIT);
        branchBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        branchBtn.setFont(new Font("Inter", Font.PLAIN, 12));
        add(branchBtn);

        add(Box.createHorizontalGlue());

        // Play Controls
        JButton playBtn = new JButton(Icons.PLAY);
        playBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        playBtn.addActionListener(e -> onPlay.run());
        add(playBtn);
        
        JButton stopBtn = new JButton(Icons.STOP);
        stopBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        stopBtn.addActionListener(e -> onStop.run());
        add(stopBtn);

        add(Box.createHorizontalStrut(120)); // Space for window controls (min/max/close)
    }

    private JMenu createMenu(String title) {
        JMenu menu = new JMenu(title);
        menu.setFont(new Font("Inter", Font.PLAIN, 12));
        return menu;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // The blue glow effect from the old toolbar
        int centerX = 220; 
        int centerY = getHeight() / 2;
        int radiusX = 350;
        int radiusY = getHeight() * 2;

        float[] dist = {0.0f, 0.3f, 1.0f};
        Color glowColor = new Color(52, 152, 219); 
        Color[] colors = {
            new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 80),
            new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 30),
            new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 0)
        };
        
        RadialGradientPaint p = new RadialGradientPaint(centerX, centerY, radiusX, dist, colors);
        g2.setPaint(p);
        g2.fillOval(centerX - radiusX, centerY - radiusY, radiusX * 2, radiusY * 2);

        g2.dispose();
    }
}

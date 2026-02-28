package com.parafield.storming.ui.widgets;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.parafield.storming.ui.utils.UIUtils;
import javax.swing.*;
import java.awt.*;

public class StormingMenuBar extends JMenuBar {
    private float alpha = 1.0f;
    private final Runnable onPlay;
    private final Runnable onStop;

    public StormingMenuBar(Runnable onPlay, Runnable onStop) {
        this.onPlay = onPlay;
        this.onStop = onStop;
        
        setOpaque(false);
        setBorder(null);
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
        
        // App Icon (exe-icon.png)
        JLabel appLogo = new JLabel(Icons.EXE_ICON);
        add(appLogo);
        add(Box.createHorizontalStrut(5));

        // Hamburger Menu (Contains File, Edit, View, etc.)
        JButton menuBtn = new JButton(Icons.HAMBURGER);
        menuBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        menuBtn.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            
            // File Submenu
            JMenu fileMenu = new JMenu("File");
            fileMenu.add(new JMenuItem("New Project..."));
            fileMenu.add(new JMenuItem("Open Project..."));
            fileMenu.addSeparator();
            fileMenu.add(new JMenuItem("Save Scene"));
            menu.add(fileMenu);

            // Edit Submenu
            JMenu editMenu = new JMenu("Edit");
            editMenu.add(new JMenuItem("Undo"));
            editMenu.add(new JMenuItem("Redo"));
            menu.add(editMenu);

            // View Submenu
            JMenu viewMenu = new JMenu("View");
            viewMenu.add(new JCheckBoxMenuItem("Hierarchy", true));
            viewMenu.add(new JCheckBoxMenuItem("Inspector", true));
            menu.add(viewMenu);

            menu.addSeparator();
            menu.add(new JMenuItem("Settings..."));
            menu.add(new JMenuItem("Exit"));

            menu.show(menuBtn, 0, menuBtn.getHeight());
        });
        add(menuBtn);
        
        add(Box.createHorizontalStrut(12));

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
                g2.setFont(UIUtils.getFont(Font.BOLD, 11f));
                String letter = projectName.substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(letter, x + (18 - fm.stringWidth(letter)) / 2, y + ((18 - fm.getHeight()) / 2) + fm.getAscent());
                g2.dispose();
            }
            @Override public int getIconWidth() { return 18; }
            @Override public int getIconHeight() { return 18; }
        });
        projectBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        add(projectBtn);
        
        add(Box.createHorizontalStrut(10));
        
        JButton branchBtn = new JButton("master", Icons.GIT);
        branchBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
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

        add(Box.createHorizontalStrut(5));
        JButton settingsBtn = new JButton(Icons.SETTINGS);
        settingsBtn.setToolTipText("Settings");
        settingsBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        add(settingsBtn);
    }

    private JMenu createMenu(String title) {
        JMenu menu = new JMenu(title);
        return menu;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (alpha < 1.0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        // Linear Gradient for a perfectly smooth fade from the absolute left
        Window win = SwingUtilities.getWindowAncestor(this);
        int w = (win != null) ? win.getWidth() : getWidth();
        
        // Find our offset relative to the window to ensure gradient starts at 0
        int xOffset = 0;
        if (win != null) {
            Point pInWin = SwingUtilities.convertPoint(this, 0, 0, win);
            xOffset = -pInWin.x;
        }

        Color glowColor = new Color(52, 152, 219);
        LinearGradientPaint p = new LinearGradientPaint(
            xOffset, 0, xOffset + Math.max(1, w), 0,
            new float[]{0.0f, 0.4f, 1.0f},
            new Color[]{
                new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 70),
                new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 25),
                new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 0)
            }
        );
        
        g2.setPaint(p);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Subtle bottom border
        g2.setColor(new Color(255, 255, 255, 15));
        g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);

        g2.dispose();
    }
}

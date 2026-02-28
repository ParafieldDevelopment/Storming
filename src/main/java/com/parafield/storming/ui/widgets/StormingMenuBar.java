package com.parafield.storming.ui.widgets;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.parafield.storming.ui.utils.UIUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class StormingMenuBar extends JMenuBar {
    private float alpha = 1.0f;
    private final Runnable onPlay;
    private final Runnable onStop;
    
    private boolean expanded = false;
    private final List<JMenu> menus = new ArrayList<>();
    private JButton projectBtn;
    private JButton branchBtn;
    private Component projectStrut;
    private Component branchStrut;

    public StormingMenuBar(Runnable onPlay, Runnable onStop) {
        this.onPlay = onPlay;
        this.onStop = onStop;
        
        setOpaque(false);
        setBorder(null);
        setPreferredSize(new Dimension(0, 40));
        putClientProperty(FlatClientProperties.STYLE, "hoverBackground: #ffffff10;");
        
        initMenuBar();
        setupClickOutDetection();
    }

    private void setupClickOutDetection() {
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof MouseEvent me && me.getID() == MouseEvent.MOUSE_PRESSED) {
                    if (expanded && !SwingUtilities.isDescendingFrom(me.getComponent(), StormingMenuBar.this)) {
                        // Check if a menu is currently showing its popup
                        boolean anyMenuShowing = false;
                        for (JMenu m : menus) {
                            if (m.isPopupMenuVisible()) {
                                anyMenuShowing = true;
                                break;
                            }
                        }
                        if (!anyMenuShowing) {
                            SwingUtilities.invokeLater(() -> setMenuExpanded(false));
                        }
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

    private void setMenuExpanded(boolean expanded) {
        this.expanded = expanded;
        for (JMenu m : menus) {
            m.setVisible(expanded);
        }
        projectBtn.setVisible(!expanded);
        branchBtn.setVisible(!expanded);
        projectStrut.setVisible(!expanded);
        branchStrut.setVisible(!expanded);
        revalidate();
        repaint();
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

        // Hamburger Menu
        JButton menuBtn = new JButton(Icons.HAMBURGER);
        menuBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        menuBtn.addActionListener(e -> setMenuExpanded(!expanded));
        add(menuBtn);
        
        add(Box.createHorizontalStrut(12));

        // Main Menus (Hidden by default)
        menus.add(createFileMenu());
        menus.add(createEditMenu());
        menus.add(createMenu("View"));
        menus.add(createMenu("Navigate"));
        menus.add(createMenu("Code"));
        menus.add(createMenu("Build"));
        menus.add(createMenu("Run"));
        menus.add(createMenu("Git"));
        menus.add(createMenu("Window"));
        menus.add(createMenu("Help"));

        for (JMenu m : menus) {
            m.setVisible(false);
            m.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 12f));
            add(m);
        }

        // Project Info (Center-ish)
        String projectName = "New Adventure";
        projectStrut = Box.createHorizontalStrut(10);
        add(projectStrut);
        
        projectBtn = new JButton(projectName, new Icon() {
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
        
        branchStrut = Box.createHorizontalStrut(10);
        add(branchStrut);
        
        branchBtn = new JButton("master", Icons.GIT);
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

    private JMenu createFileMenu() {
        JMenu menu = new JMenu("File");
        menu.add(new JMenuItem("New Project...", Icons.PLUS));
        menu.add(new JMenuItem("Open Project...", Icons.FOLDER));
        menu.addSeparator();
        menu.add(new JMenuItem("Save Scene", Icons.CLIPBOARD));
        menu.addSeparator();
        menu.add(new JMenuItem("Settings...", Icons.SETTINGS));
        menu.add(new JMenuItem("Exit"));
        return menu;
    }

    private JMenu createEditMenu() {
        JMenu menu = new JMenu("Edit");
        menu.add(new JMenuItem("Undo"));
        menu.add(new JMenuItem("Redo"));
        menu.addSeparator();
        menu.add(new JMenuItem("Cut"));
        menu.add(new JMenuItem("Copy"));
        menu.add(new JMenuItem("Paste"));
        return menu;
    }

    private JMenu createMenu(String title) {
        JMenu menu = new JMenu(title);
        menu.add(new JMenuItem("Placeholder Action"));
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

        // Focused Linear Glow
        Color glowColor = new Color(52, 152, 219);
        LinearGradientPaint p = new LinearGradientPaint(
            0, 0, 500, 0,
            new float[]{0.0f, 0.15f, 0.5f, 1.0f},
            new Color[]{
                new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 0),
                new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 70),
                new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 25),
                new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 0)
            }
        );
        
        g2.setPaint(p);
        g2.fillRect(0, 0, 500, getHeight());

        // Subtle bottom border
        g2.setColor(new Color(255, 255, 255, 15));
        g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);

        g2.dispose();
    }
}

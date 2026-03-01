package com.parafield.storming.ui.widgets;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;

/**
 * A JetBrains-style sidebar used to host tool window buttons and shortcuts.
 * Supports both vertical and horizontal orientations and alpha-fade animations.
 * Features specialized toggle buttons with selection indicators.
 */
public class SideBar extends JPanel {
    private final int barWidth;
    private float alpha = 1.0f;

    /**
     * Sets the alpha transparency of the sidebar for animation purposes.
     * @param alpha The alpha value (0.0 to 1.0).
     */
    public void setAlpha(float alpha) {
        this.alpha = alpha;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        if (alpha < 1.0f) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paint(g2);
            g2.dispose();
        } else {
            super.paint(g);
        }
    }

    /**
     * Constructs a SideBar.
     * @param orientation The orientation of the sidebar (SwingConstants.VERTICAL or SwingConstants.HORIZONTAL).
     * @param width The width (for vertical) or height (for horizontal) of the sidebar.
     */
    public SideBar(int orientation, int width) {
        this.barWidth = width;
        setLayout(new BoxLayout(this, orientation == SwingConstants.VERTICAL ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));
        setPreferredSize(new Dimension(barWidth, 0));
        setBackground(UIManager.getColor("Panel.background"));
        add(Box.createVerticalStrut(5));
    }

    /**
     * Adds a new tab/button to the sidebar.
     * @param name The name of the tab (used as a tooltip).
     * @param icon The icon to display on the button.
     * @param isToggle true if the button should behave as a toggle button (persistent selection).
     * @param onSelect Callback to execute when the button is clicked.
     * @return The created AbstractButton.
     */
    public AbstractButton addTab(String name, Icon icon, boolean isToggle, Runnable onSelect) {
        AbstractButton btn;
        if (isToggle) {
            btn = new JToggleButton(icon) {
                @Override
                protected void paintComponent(Graphics g) {
                    if (isSelected()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(52, 152, 219, 30)); // Subtle blue background
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        
                        g2.setColor(new Color(52, 152, 219)); // Bright blue indicator
                        if (barWidth < 50) { // Vertical sidebar
                            g2.fillRect(0, 8, 2, getHeight() - 16);
                        } else { // Horizontal (if used)
                            g2.fillRect(8, getHeight() - 2, getWidth() - 16, 2);
                        }
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            };
        } else {
            btn = new JButton(icon);
        }

        btn.setToolTipText(name);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btn.setPreferredSize(new Dimension(barWidth, barWidth));
        btn.setMaximumSize(new Dimension(barWidth, barWidth));
        btn.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 14f));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btn.addActionListener(e -> onSelect.run());

        add(btn);
        add(Box.createVerticalStrut(2));
        return btn;
    }

    /**
     * Adds a visual separator line to the sidebar.
     */
    public void addSeparator() {
        JPanel sep = new JPanel();
        sep.setMaximumSize(new Dimension(barWidth - 14, 1));
        sep.setPreferredSize(new Dimension(barWidth - 14, 1));
        sep.setBackground(UIManager.getColor("Component.borderColor"));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        add(Box.createVerticalStrut(6));
        add(sep);
        add(Box.createVerticalStrut(6));
    }

    /**
     * Adds flexible space (glue) to the sidebar, pushing subsequent components to the end.
     */
    public void addGlue() {
        add(Box.createVerticalGlue());
    }
}

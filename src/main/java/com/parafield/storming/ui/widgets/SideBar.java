package com.parafield.storming.ui.widgets;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;

public class SideBar extends JPanel {
    private final ButtonGroup group = new ButtonGroup();
    private final int barWidth;
    private float alpha = 1.0f;

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

    public SideBar(int orientation, int width) {
        this.barWidth = width;
        setLayout(new BoxLayout(this, orientation == SwingConstants.VERTICAL ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));
        setPreferredSize(new Dimension(barWidth, 0));
        setBackground(UIManager.getColor("Panel.background"));
        add(Box.createVerticalStrut(5));
    }

    public AbstractButton addTab(String name, Icon icon, boolean isToggle, Runnable onSelect) {
        AbstractButton btn;
        if (isToggle) {
            btn = new JToggleButton(icon) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (isSelected()) {
                        g.setColor(new Color(52, 152, 219)); // Blue indicator
                        if (barWidth < 50) { // Vertical sidebar
                            g.fillRect(0, 8, 2, getHeight() - 16);
                        } else { // Horizontal (if used)
                            g.fillRect(8, getHeight() - 2, getWidth() - 16, 2);
                        }
                    }
                }
            };
            group.add(btn);
        } else {
            btn = new JButton(icon);
        }

        btn.setToolTipText(name);
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
}

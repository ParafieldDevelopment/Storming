package com.parafield.storming.ui.panels;

import com.parafield.storming.Icons;
import javax.swing.*;
import java.awt.*;

/**
 * Displays user notifications, system alerts, and engine messages.
 * Features a clean interface with a fallback message when no notifications are present
 * and a stylized background icon.
 */
public class NotificationsPanel extends JPanel {
    /**
     * Constructs a NotificationsPanel with a default empty state.
     */
    public NotificationsPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("No new notifications.", SwingConstants.CENTER);
        label.setForeground(UIManager.getColor("Label.disabledForeground"));
        add(label, BorderLayout.CENTER);
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
        
        int size = 80;
        int x = getWidth() - size - 25;
        int y = getHeight() - size - 25;
        if (Icons.BELL_80 != null) {
            Icons.BELL_80.paintIcon(this, g2, x, y);
        }
        g2.dispose();
    }
}

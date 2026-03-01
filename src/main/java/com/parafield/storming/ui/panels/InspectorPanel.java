package com.parafield.storming.ui.panels;

import com.parafield.storming.Icons;
import javax.swing.*;
import java.awt.*;

/**
 * Provides a property editor for the currently selected object in the scene.
 * [WIP] This component is currently a visual placeholder and will be bound to
 * the engine's object properties in future development phases.
 */
public class InspectorPanel extends JPanel {
    /**
     * Constructs an InspectorPanel with a prompt to select an object.
     */
    public InspectorPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Select an object...", SwingConstants.CENTER);
        label.setForeground(UIManager.getColor("Label.disabledForeground"));
        add(label, BorderLayout.CENTER);
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f)); // Slightly less opacity
        
        int size = 80;
        int x = getWidth() - size - 25;
        int y = getHeight() - size - 25;
        if (Icons.SEARCH_80 != null) {
            Icons.SEARCH_80.paintIcon(this, g2, x, y);
        }
        g2.dispose();
    }
}

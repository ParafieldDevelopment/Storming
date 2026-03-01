package com.parafield.storming.ui.panels;

import com.parafield.storming.Icons;
import javax.swing.*;
import java.awt.*;

/**
 * Analyzes project scripts and assets for errors, performance bottlenecks, and best practices.
 * [WIP] This component is currently a visual placeholder. 
 * Future versions will provide actionable insights for developers.
 */
public class AnalyzerPanel extends JPanel {
    /**
     * Constructs an AnalyzerPanel with a placeholder header.
     */
    public AnalyzerPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Script & Asset Analyzer", SwingConstants.CENTER);
        label.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 14f));
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
        if (Icons.WARN_80 != null) {
            Icons.WARN_80.paintIcon(this, g2, x, y);
        }
        g2.dispose();
    }
}

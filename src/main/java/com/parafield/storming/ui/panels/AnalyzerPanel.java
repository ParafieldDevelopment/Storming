package com.parafield.storming.ui.panels;

import com.parafield.storming.Icons;
import javax.swing.*;
import java.awt.*;

public class AnalyzerPanel extends JPanel {
    public AnalyzerPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Script & Asset Analyzer", SwingConstants.CENTER);
        label.setFont(new Font("Inter", Font.BOLD, 14));
        label.setForeground(UIManager.getColor("Label.disabledForeground"));
        add(label, BorderLayout.CENTER);
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
        
        int size = 64;
        int x = getWidth() - size - 25;
        int y = getHeight() - size - 25;
        g2.drawImage(Icons.WARN_64.getImage(), x, y, size, size, null);
        g2.dispose();
    }
}

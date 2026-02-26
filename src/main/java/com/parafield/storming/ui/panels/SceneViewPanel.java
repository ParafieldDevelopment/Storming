package com.parafield.storming.ui.panels;

import javax.swing.*;
import java.awt.*;

public class SceneViewPanel extends JPanel {

    public SceneViewPanel() {
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(20, 20, 20));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw a very subtle grid
        g2.setColor(new Color(40, 40, 40, 100));
        for(int i=0; i<getWidth(); i+=40) g2.drawLine(i, 0, i, getHeight());
        for(int i=0; i<getHeight(); i+=40) g2.drawLine(0, i, getWidth(), i);
        
        g2.setColor(Color.GRAY);
        g2.drawString("Scene View (Engine Window will render here)", 20, 30);
    }
}

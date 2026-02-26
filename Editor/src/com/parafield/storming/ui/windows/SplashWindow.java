package com.parafield.storming.ui.windows;

import javax.swing.*;
import java.awt.*;

public class SplashWindow extends JWindow {

    public SplashWindow(String imagePath) {
        try {
            ImageIcon original = new ImageIcon(imagePath);
            Image img = original.getImage();
            
            // Scaled to a professional size
            int width = 800;
            int height = (int) (img.getHeight(null) * (800.0 / img.getWidth(null)));
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            
            getContentPane().add(new JLabel(new ImageIcon(scaled)));
            pack();
            setLocationRelativeTo(null);
        } catch (Exception e) {
            System.err.println("Splash Error: " + e.getMessage());
        }
    }

    public void showSplash() {
        setVisible(true);
    }

    public void close() {
        dispose();
    }
}

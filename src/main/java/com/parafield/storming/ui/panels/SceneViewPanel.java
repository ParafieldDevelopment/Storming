package com.parafield.storming.ui.panels;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import java.awt.Canvas;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;

public class SceneViewPanel extends JPanel {

    private final Canvas canvas;

    public SceneViewPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 46)); // Match "Storming Blue"

        canvas = new Canvas();
        canvas.setBackground(new Color(30, 30, 46));
        add(canvas, BorderLayout.CENTER);
    }

    public long getNativeWindowID() {
        if (!canvas.isDisplayable()) {
            return 0;
        }
        
        try {
            if (Platform.isLinux()) {
                // Force the component to be realized
                canvas.getGraphics().dispose();
                return Native.getComponentID(canvas);
            }
        } catch (Exception e) {
            System.err.println("[System] Failed to get Native ID: " + e.getMessage());
        }
        return 0;
    }
}

package com.parafield.storming.ui.widgets;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import java.awt.*;

public class MainToolbar extends JToolBar {

    public MainToolbar(Runnable onPlay, Runnable onStop) {
        setFloatable(false);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
        setPreferredSize(new Dimension(0, 40));
        
        // Left part: Hamburger Menu
        add(Box.createHorizontalStrut(5));
        JButton menuBtn = new JButton(Icons.HAMBURGER);
        menuBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        menuBtn.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            menu.add(new JMenuItem("File"));
            menu.add(new JMenuItem("Edit"));
            menu.add(new JMenuItem("View"));
            menu.addSeparator();
            menu.add(new JMenuItem("Help"));
            menu.show(menuBtn, 0, menuBtn.getHeight());
        });
        add(menuBtn);
        
        add(Box.createHorizontalStrut(10));
        JLabel projectLabel = new JLabel("Storming Project: New Adventure");
        projectLabel.setFont(new Font("Inter", Font.BOLD, 12));
        add(projectLabel);
        
        add(Box.createHorizontalGlue());

        // Right part: Run/Play controls
        JPanel runControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        runControls.setOpaque(false);
        
        JButton playBtn = new JButton(Icons.PLAY);
        playBtn.setToolTipText("Run (F5)");
        playBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        playBtn.addActionListener(e -> onPlay.run());
        
        JButton stopBtn = new JButton(Icons.STOP);
        stopBtn.setToolTipText("Stop (Shift+F5)");
        stopBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        stopBtn.addActionListener(e -> onStop.run());

        runControls.add(playBtn);
        runControls.add(stopBtn);
        
        add(runControls);
        add(Box.createHorizontalStrut(10));
        
        JLabel systemInfo = new JLabel("v0.1.0-alpha ");
        systemInfo.setForeground(UIManager.getColor("Label.disabledForeground"));
        add(systemInfo);
        add(Box.createHorizontalStrut(15));
    }
}

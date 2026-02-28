package com.parafield.storming.ui.widgets;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ToolWindow extends JPanel {
    private final String title;
    private final JComponent content;
    private final JPanel header;

    public ToolWindow(String title, JComponent content) {
        this.title = title;
        this.content = content;
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, UIManager.getColor("Component.borderColor")));

        header = new JPanel(new BorderLayout());
        header.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 3%)");
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
        header.setPreferredSize(new Dimension(0, 28));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 10f));
        titleLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        titleLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        
        header.add(titleLabel, BorderLayout.WEST);
        
        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        actions.setOpaque(false);
        
        JButton optionsBtn = new JButton(Icons.SETTINGS); // Reuse settings icon
        optionsBtn.setToolTipText("Options");
        optionsBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        optionsBtn.setPreferredSize(new Dimension(24, 24));
        
        JButton hideBtn = new JButton("−"); // Em-dash for hide
        hideBtn.setToolTipText("Hide");
        hideBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        hideBtn.setPreferredSize(new Dimension(24, 24));
        
        actions.add(optionsBtn);
        actions.add(hideBtn);
        
        header.add(actions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }
}

package com.parafield.storming.ui.widgets;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SideBar extends JPanel {
    private final List<JToggleButton> buttons = new ArrayList<>();
    private final ButtonGroup group = new ButtonGroup();

    public SideBar(int orientation) {
        setLayout(new BoxLayout(this, orientation == SwingConstants.VERTICAL ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));
        setPreferredSize(new Dimension(40, 0));
    }

    public void addTab(String name, Icon icon, Runnable onSelect) {
        JToggleButton btn = new JToggleButton(icon);
        if (icon == null) {
            btn.setText(name.substring(0, 1).toUpperCase());
        }
        btn.setToolTipText(name);
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btn.setPreferredSize(new Dimension(40, 40));
        btn.setMaximumSize(new Dimension(40, 40));
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        
        btn.addActionListener(e -> {
            onSelect.run();
        });

        buttons.add(btn);
        add(btn);
    }
}

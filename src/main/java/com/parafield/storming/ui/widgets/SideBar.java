package com.parafield.storming.ui.widgets;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SideBar extends JPanel {
    private final ButtonGroup group = new ButtonGroup();

    public SideBar(int orientation) {
        setLayout(new BoxLayout(this, orientation == SwingConstants.VERTICAL ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));
        setPreferredSize(new Dimension(48, 0));
    }

    public AbstractButton addTab(String name, Icon icon, boolean isToggle, Runnable onSelect) {
        AbstractButton btn;
        if (isToggle) {
            btn = new JToggleButton(icon);
            group.add(btn);
        } else {
            btn = new JButton(icon);
        }

        if (icon == null) {
            btn.setText(name.substring(0, 1).toUpperCase());
        }
        
        btn.setToolTipText(name);
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btn.setPreferredSize(new Dimension(48, 48));
        btn.setMaximumSize(new Dimension(48, 48));
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        
        btn.addActionListener(e -> onSelect.run());

        add(btn);
        return btn;
    }
}

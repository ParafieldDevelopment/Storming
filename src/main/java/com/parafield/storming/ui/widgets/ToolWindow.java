package com.parafield.storming.ui.widgets;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A standardized container for tool panels within the editor (e.g., Inspector, Hierarchy).
 * Includes a stylized header with a title and action buttons (Options, Hide).
 */
public class ToolWindow extends JPanel {
    private final String title;
    private final JComponent content;
    private final JPanel header;
    private final Runnable onHide;

    /**
     * Constructs a ToolWindow.
     * @param title The display title for the tool window.
     * @param content The main content component to be displayed.
     * @param onHide A callback to execute when the 'Hide' button is clicked.
     */
    public ToolWindow(String title, JComponent content, Runnable onHide) {
        this.title = title;
        this.content = content;
        this.onHide = onHide;
        
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
        if (onHide != null) {
            hideBtn.addActionListener(e -> onHide.run());
        }
        
        actions.add(optionsBtn);
        actions.add(hideBtn);
        
        header.add(actions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        
        JPanel contentContainer = new JPanel(new BorderLayout());
        contentContainer.setOpaque(false);
        contentContainer.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentContainer.add(content, BorderLayout.CENTER);
        
        add(contentContainer, BorderLayout.CENTER);
    }
}

package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PRDetailsSidePanel extends JPanel {

    private final Runnable onBack;
    private final JPanel content;

    public PRDetailsSidePanel(String title, Runnable onBack) {
        this.onBack = onBack;
        setLayout(new BorderLayout());
        
        // --- 1. Header (Back Button) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
        header.setPreferredSize(new Dimension(0, 32));
        header.setBackground(UIManager.getColor("Panel.background"));

        JButton backBtn = new JButton("← Back");
        backBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        backBtn.addActionListener(e -> onBack.run());
        header.add(backBtn, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // --- 2. Changed Files & Checks ---
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel titleLbl = new JLabel("Files Changed");
        titleLbl.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 11f));
        titleLbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        content.add(titleLbl);
        content.add(Box.createVerticalStrut(10));

        addFileItem("src/main/ui/PRPanel.java", "+42 -12");
        addFileItem("src/main/ui/MainWindow.java", "+15 -5");
        addFileItem("Engine/Core/Log.cpp", "+2 -2");

        content.add(Box.createVerticalStrut(20));
        
        JLabel checkLbl = new JLabel("Checks (3 Passed)");
        checkLbl.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 11f));
        checkLbl.setForeground(new Color(46, 204, 113));
        content.add(checkLbl);
        
        addCheckItem("Build", true);
        addCheckItem("Unit Tests", true);
        addCheckItem("Linter", true);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addFileItem(String name, String diff) {
        JPanel item = new JPanel(new BorderLayout());
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        item.setOpaque(false);
        
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        JLabel diffLbl = new JLabel(diff);
        diffLbl.setFont(new Font("Monospaced", Font.BOLD, 11));
        diffLbl.setForeground(new Color(46, 204, 113));

        item.add(nameLbl, BorderLayout.CENTER);
        item.add(diffLbl, BorderLayout.EAST);
        
        content.add(item);
    }

    private void addCheckItem(String name, boolean success) {
        JLabel lbl = new JLabel("  ✓ " + name);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(UIManager.getColor("Label.foreground"));
        content.add(lbl);
    }
}

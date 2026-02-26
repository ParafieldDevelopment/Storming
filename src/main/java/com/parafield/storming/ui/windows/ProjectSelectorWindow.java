package com.parafield.storming.ui.windows;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProjectSelectorWindow extends JFrame {

    public ProjectSelectorWindow() {
        setTitle("Storming Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        // Modern look
        rootPane.putClientProperty("apple.awt.fullWindowContent", true);
        rootPane.putClientProperty("apple.awt.transparentTitleBar", true);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // --- LEFT SIDEBAR (Recent Projects) ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 2%)");
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));

        JLabel recentTitle = new JLabel("Projects");
        recentTitle.setFont(new Font("Inter", Font.BOLD, 13));
        recentTitle.setBorder(new EmptyBorder(25, 20, 10, 20));
        sidebar.add(recentTitle, BorderLayout.NORTH);

        // List of projects (placeholder)
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement("  Storming Demo (2D)");
        listModel.addElement("  Test Project");
        JList<String> projectList = new JList<>(listModel);
        projectList.setOpaque(false);
        projectList.setFixedCellHeight(35);
        projectList.setFont(new Font("Inter", Font.PLAIN, 12));
        projectList.setBorder(new EmptyBorder(0, 10, 0, 10));
        
        sidebar.add(new JScrollPane(projectList), BorderLayout.CENTER);

        // Sidebar Footer
        JPanel sidebarFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        sidebarFooter.setOpaque(false);
        
        JButton settingsBtn = new JButton(new com.formdev.flatlaf.extras.FlatSVGIcon("com/parafield/storming/icons/settings.svg", 20, 20));
        settingsBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        settingsBtn.setToolTipText("Settings");
        settingsBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Settings coming soon!"));
        
        sidebarFooter.add(settingsBtn);
        sidebar.add(sidebarFooter, BorderLayout.SOUTH);

        root.add(sidebar, BorderLayout.WEST);

        // --- RIGHT CONTENT AREA ---
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setBorder(new EmptyBorder(20, 40, 20, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // Welcome Header
        JLabel welcome = new JLabel("Storming");
        welcome.setFont(new Font("Inter", Font.BOLD, 48));
        mainContent.add(welcome, gbc);

        gbc.gridy++;
        JLabel version = new JLabel("v2026.1 Preview");
        version.setForeground(UIManager.getColor("Label.disabledForeground"));
        version.setBorder(new EmptyBorder(0, 0, 40, 0));
        mainContent.add(version, gbc);

        // Action Buttons Panel
        gbc.gridy++;
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        actionPanel.setOpaque(false);
        
        JButton newBtn = createBigButton("New Project", "Primary");
        newBtn.addActionListener(e -> launchMainEditor());
        
        JButton openBtn = createBigButton("Open", "Default");
        
        actionPanel.add(newBtn);
        actionPanel.add(openBtn);
        mainContent.add(actionPanel, gbc);

        root.add(mainContent, BorderLayout.CENTER);
    }

    private JButton createBigButton(String text, String type) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setFont(new Font("Inter", Font.BOLD, 13));
        if (type.equals("Primary")) {
            btn.putClientProperty(FlatClientProperties.STYLE, "background: #36598a; foreground: #ffffff; arc: 8;");
        } else {
            btn.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        }
        return btn;
    }

    private void launchMainEditor() {
        dispose();
        new MainWindow().setVisible(true);
    }
}

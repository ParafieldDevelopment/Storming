package com.parafield.storming.ui.windows;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProjectSelectorWindow extends JFrame {

    public ProjectSelectorWindow() {
        setIconImage(Icons.FRAME_ICON);
        setTitle("Storming Engine Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        
        rootPane.putClientProperty("apple.awt.fullWindowContent", true);
        rootPane.putClientProperty("apple.awt.transparentTitleBar", true);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // --- LEFT SIDEBAR ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(300, 0));
        sidebar.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 2%)");
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));

        JLabel titleLabel = new JLabel("Projects");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 16));
        titleLabel.setBorder(new EmptyBorder(30, 25, 15, 25));
        sidebar.add(titleLabel, BorderLayout.NORTH);

        // Project List
        DefaultListModel<ProjectItem> listModel = new DefaultListModel<>();
        listModel.addElement(new ProjectItem("Storming Demo (2D)", "/home/user/storming/demo"));
        listModel.addElement(new ProjectItem("New Adventure", "/home/user/projects/game1"));
        
        JList<ProjectItem> projectList = new JList<>(listModel);
        projectList.setCellRenderer(new ProjectListRenderer());
        projectList.setOpaque(false);
        projectList.setFixedCellHeight(60);
        projectList.setBorder(new EmptyBorder(0, 5, 0, 5));
        
        JScrollPane scrollPane = new JScrollPane(projectList);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        sidebar.add(scrollPane, BorderLayout.CENTER);

        // Sidebar Footer
        JPanel sidebarFooter = new JPanel(new BorderLayout());
        sidebarFooter.setOpaque(false);
        sidebarFooter.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        JButton settingsBtn = new JButton(" Settings", Icons.SETTINGS);
        settingsBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        settingsBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Settings coming soon!"));
        sidebarFooter.add(settingsBtn, BorderLayout.WEST);
        
        sidebar.add(sidebarFooter, BorderLayout.SOUTH);
        root.add(sidebar, BorderLayout.WEST);

        // --- MAIN CONTENT (Welcome Screen) ---
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setBackground(UIManager.getColor("Panel.background"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);

        // Big Logo
        JLabel logoLabel = new JLabel(Icons.LOGO);
        mainContent.add(logoLabel, gbc);

        // Title
        gbc.gridy++;
        JLabel welcomeTitle = new JLabel("Storming Engine");
        welcomeTitle.setFont(new Font("Inter", Font.BOLD, 32));
        mainContent.add(welcomeTitle, gbc);

        // Subtitle
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 40, 0);
        JLabel subtitle = new JLabel("High Performance 2D/3D Game Development");
        subtitle.setForeground(UIManager.getColor("Label.disabledForeground"));
        mainContent.add(subtitle, gbc);

        // Action Buttons
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actions.setOpaque(false);

        JButton newProjectBtn = createActionButton("New Project", "#3498db", true);
        newProjectBtn.addActionListener(e -> launchMainEditor());
        
        JButton openProjectBtn = createActionButton("Open Project", null, false);
        
        actions.add(newProjectBtn);
        actions.add(openProjectBtn);
        mainContent.add(actions, gbc);

        // Version Info
        gbc.gridy++;
        gbc.insets = new Insets(50, 0, 0, 0);
        JLabel versionLabel = new JLabel("v2026.1 Alpha Preview");
        versionLabel.setFont(new Font("Inter", Font.PLAIN, 11));
        versionLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        mainContent.add(versionLabel, gbc);

        root.add(mainContent, BorderLayout.CENTER);
    }

    private JButton createActionButton(String text, String colorHex, boolean primary) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(180, 45));
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        
        String style = "arc: 10;";
        if (primary && colorHex != null) {
            style += "background: " + colorHex + "; foreground: #ffffff; borderWidth: 0;";
        }
        btn.putClientProperty(FlatClientProperties.STYLE, style);
        return btn;
    }

    private void launchMainEditor() {
        dispose();
        new MainWindow().setVisible(true);
    }

    // Inner classes for the list
    private static class ProjectItem {
        String name, path;
        ProjectItem(String name, String path) { this.name = name; this.path = path; }
    }

    private static class ProjectListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            ProjectItem item = (ProjectItem) value;
            JPanel panel = new JPanel(new BorderLayout(15, 0));
            panel.setBorder(new EmptyBorder(10, 15, 10, 15));
            panel.setOpaque(isSelected);
            if (isSelected) panel.setBackground(UIManager.getColor("List.selectionBackground"));

            JLabel nameLabel = new JLabel(item.name);
            nameLabel.setFont(new Font("Inter", Font.BOLD, 13));
            nameLabel.setForeground(isSelected ? UIManager.getColor("List.selectionForeground") : UIManager.getColor("Label.foreground"));
            
            JLabel pathLabel = new JLabel(item.path);
            pathLabel.setFont(new Font("Inter", Font.PLAIN, 11));
            pathLabel.setForeground(isSelected ? UIManager.getColor("List.selectionForeground") : UIManager.getColor("Label.disabledForeground"));

            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            textPanel.add(nameLabel);
            textPanel.add(pathLabel);

            panel.add(new JLabel(Icons.FOLDER), BorderLayout.WEST);
            panel.add(textPanel, BorderLayout.CENTER);

            return panel;
        }
    }
}

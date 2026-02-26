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
        
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(30, 30, 30));
        setContentPane(root);

        // --- LEFT SIDEBAR (Recent Projects) ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(300, 0));
        sidebar.setBackground(new Color(35, 35, 35));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(50, 50, 50)));

        JLabel recentTitle = new JLabel("Recent Projects");
        recentTitle.setFont(new Font("Inter", Font.BOLD, 12));
        recentTitle.setBorder(new EmptyBorder(20, 20, 10, 20));
        sidebar.add(recentTitle, BorderLayout.NORTH);

        // Placeholder for empty state
        JPanel emptyState = new JPanel(new GridBagLayout());
        emptyState.setOpaque(false);
        JLabel noProjects = new JLabel("No recent projects");
        noProjects.setForeground(Color.GRAY);
        emptyState.add(noProjects);
        sidebar.add(emptyState, BorderLayout.CENTER);

        // --- SIDEBAR FOOTER (Settings) ---
        JPanel sidebarFooter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sidebarFooter.setOpaque(false);
        sidebarFooter.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JButton settingsBtn = new JButton(new com.formdev.flatlaf.extras.FlatSVGIcon("icons/settings.svg", 20, 20));
        settingsBtn.setToolTipText("Editor Settings");
        settingsBtn.putClientProperty("FlatLaf.style", "arc: 8;");
        settingsBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Editor Settings coming soon!"));
        
        sidebarFooter.add(settingsBtn);
        sidebar.add(sidebarFooter, BorderLayout.SOUTH);

        root.add(sidebar, BorderLayout.WEST);

        // --- RIGHT CONTENT AREA ---
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(40, 60, 40, 60));

        // Welcome Header
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        JLabel welcome = new JLabel("Storming Engine");
        welcome.setFont(new Font("Inter", Font.BOLD, 32));
        JLabel version = new JLabel("v2026.001.000 Development Branch");
        version.setForeground(new Color(120, 120, 120));
        header.add(welcome);
        header.add(version);
        mainContent.add(header, BorderLayout.NORTH);

        // Action Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 20));
        actions.setOpaque(false);
        
        JButton newBtn = createActionButton("New Project", "Start a new 2D or 3D adventure");
        newBtn.addActionListener(e -> launchMainEditor());
        
        JButton openBtn = createActionButton("Open", "Open an existing .storm file from disk");
        
        actions.add(newBtn);
        actions.add(Box.createHorizontalStrut(20));
        actions.add(openBtn);
        
        mainContent.add(actions, BorderLayout.CENTER);

        // Footer
        JLabel copyright = new JLabel("© 2026 Parafield Studios");
        copyright.setForeground(new Color(80, 80, 80));
        copyright.setFont(new Font("Inter", Font.PLAIN, 10));
        mainContent.add(copyright, BorderLayout.SOUTH);

        root.add(mainContent, BorderLayout.CENTER);
    }

    private JButton createActionButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(160, 45));
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        
        // Use FlatLaf specific styles for a "Primary" look
        btn.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 8; " + 
            "hoverBackground: #454545; " + 
            "focusedBackground: #505050;");
            
        return btn;
    }

    private void launchMainEditor() {
        dispose();
        new MainWindow().setVisible(true);
    }
}

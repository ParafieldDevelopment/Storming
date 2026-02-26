package com.parafield.storming.ui.windows;

import com.parafield.storming.EditorApp;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProjectSelectorWindow extends JFrame {

    public ProjectSelectorWindow() {
        setTitle("Storming Engine - Project Selector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(30, 30, 30));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(root);

        // Header
        JLabel title = new JLabel("Welcome to Storming");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        root.add(title, BorderLayout.NORTH);

        // Content
        JPanel options = new JPanel(new GridLayout(1, 2, 20, 0));
        options.setOpaque(false);
        options.setBorder(new EmptyBorder(40, 0, 40, 0));

        JButton newProj = createOptionButton("New Project", "Create a fresh project from scratch");
        newProj.addActionListener(e -> launchMainEditor());

        JButton openProj = createOptionButton("Open Project", "Open an existing .storm project");

        options.add(newProj);
        options.add(openProj);
        root.add(options, BorderLayout.CENTER);
        
        // Footer (Build info)
        JLabel buildInfo = new JLabel("v2026.001.000 - Development Branch");
        buildInfo.setForeground(Color.GRAY);
        root.add(buildInfo, BorderLayout.SOUTH);
    }

    private JButton createOptionButton(String title, String desc) {
        JButton btn = new JButton("<html><div style='text-align: center;'><b style='font-size: 14px;'>" + title + "</b><br><small style='color: #888;'>" + desc + "</small></div></html>");
        btn.setPreferredSize(new Dimension(300, 200));
        btn.putClientProperty("FlatLaf.style", "arc: 12;");
        return btn;
    }

    private void launchMainEditor() {
        dispose();
        new MainWindow().setVisible(true);
    }
}

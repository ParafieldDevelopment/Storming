package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PRDetailsCenterPanel extends JPanel {

    public PRDetailsCenterPanel(String title, String author) {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));

        // --- Header Section ---
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(20, 25, 15, 25));
        header.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        
        JLabel authorLabel = new JLabel("Opened by " + author + " • 3 comments");
        authorLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        authorLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(5));
        header.add(authorLabel);

        // --- Tabs (Description / Logs) ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        
        // Description Tab
        JTextArea desc = new JTextArea("This pull request introduces the new centralized PR management system.\n\n" +
            "Summary of changes:\n" +
            "- Added PRPanel with list and detail views.\n" +
            "- Integrated with MainWindow editor tabs.\n" +
            "- Improved SideBar interaction.");
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setBorder(new EmptyBorder(10, 10, 10, 10));
        tabs.addTab("Description", new JScrollPane(desc));

        // Logs Tab
        JTextArea logs = new JTextArea("[SUCCESS] Build passed in 1m 24s\n" +
            "[SUCCESS] All unit tests passed (124/124)\n" +
            "[INFO] Deployed to staging-01");
        logs.setEditable(false);
        logs.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        logs.setBackground(new Color(20, 20, 25));
        logs.setForeground(new Color(46, 204, 113));
        logs.setBorder(new EmptyBorder(10, 10, 10, 10));
        tabs.addTab("Checks & Logs", new JScrollPane(logs));

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }
}

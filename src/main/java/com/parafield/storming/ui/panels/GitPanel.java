package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an integrated Git interface within the editor.
 * [WIP] This component is currently a visual placeholder and uses mock data.
 * Future versions will implement actual Git integration for viewing changes, 
 * managing branches, and performing commit operations.
 */
public class GitPanel extends JPanel {

    private final JPanel changesList;
    private final JTextArea commitMessage;
    private final JLabel branchLabel;

    /**
     * Constructs a GitPanel, initializing the UI and populating it with mock Git status data.
     */
    public GitPanel() {
        setLayout(new BorderLayout());
        
        // --- 1. Header (Branch Info) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
        header.setPreferredSize(new Dimension(0, 32));
        header.setBackground(UIManager.getColor("Panel.background"));

        branchLabel = new JLabel("  master", Icons.GIT, SwingConstants.LEFT);
        branchLabel.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 11f));
        header.add(branchLabel, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        actions.setOpaque(false);
        JButton refreshBtn = new JButton(Icons.RESTART); // Reuse icon
        refreshBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        refreshBtn.setToolTipText("Refresh Status");
        actions.add(refreshBtn);
        header.add(actions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // --- 2. Changes List ---
        changesList = new JPanel();
        changesList.setLayout(new BoxLayout(changesList, BoxLayout.Y_AXIS));
        changesList.setBackground(UIManager.getColor("TextArea.background"));
        
        // Mock data for now
        addChangeItem("src/main/java/MainWindow.java", "modified");
        addChangeItem("Engine/2D/src/Renderer.cpp", "modified");
        addChangeItem("assets/textures/player.png", "untracked");

        JScrollPane scrollPane = new JScrollPane(changesList);
        scrollPane.setBorder(null);
        
        // --- 3. Commit Section ---
        JPanel commitSection = new JPanel(new BorderLayout(0, 5));
        commitSection.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        commitMessage = new JTextArea();
        commitMessage.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Commit message...");
        commitMessage.setLineWrap(true);
        commitMessage.setWrapStyleWord(true);
        commitMessage.setFont(new Font("SansSerif", Font.PLAIN, 12));
        commitMessage.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 3%)");
        
        JScrollPane msgScroll = new JScrollPane(commitMessage);
        msgScroll.setPreferredSize(new Dimension(0, 80));
        msgScroll.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        JButton commitBtn = new JButton("Commit");
        commitBtn.putClientProperty(FlatClientProperties.STYLE, "background: #3498db; foreground: #ffffff; hoverBackground: #2980b9");
        
        JButton pushBtn = new JButton("Commit and Push...");
        
        btnPanel.add(commitBtn);
        btnPanel.add(pushBtn);

        commitSection.add(msgScroll, BorderLayout.CENTER);
        commitSection.add(btnPanel, BorderLayout.SOUTH);

        // --- Layout ---
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, commitSection);
        split.setDividerLocation(300);
        split.setDividerSize(3);
        split.setBorder(null);
        split.setResizeWeight(1.0);

        add(split, BorderLayout.CENTER);
    }

    /**
     * Adds a file change item to the list.
     * @param file The path to the changed file.
     * @param status The status of the change (e.g., "modified", "untracked").
     */
    private void addChangeItem(String file, String status) {
        JPanel item = new JPanel(new BorderLayout());
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        item.setPreferredSize(new Dimension(0, 28));
        item.setOpaque(false);
        item.setBorder(new EmptyBorder(0, 5, 0, 5));

        JCheckBox cb = new JCheckBox(file);
        cb.setSelected(true);
        cb.setFont(UIManager.getFont("defaultFont").deriveFont(12f));
        
        JLabel statusLbl = new JLabel(status);
        statusLbl.setFont(UIManager.getFont("defaultFont").deriveFont(Font.ITALIC, 10f));
        statusLbl.setForeground(status.equals("untracked") ? new Color(155, 89, 182) : new Color(46, 204, 113));

        item.add(cb, BorderLayout.CENTER);
        item.add(statusLbl, BorderLayout.EAST);
        
        changesList.add(item);
    }
}

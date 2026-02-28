package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PRPanel extends JPanel {

    private final JPanel prList;

    public PRPanel() {
        setLayout(new BorderLayout());
        
        // --- 1. Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
        header.setPreferredSize(new Dimension(0, 32));
        header.setBackground(UIManager.getColor("Panel.background"));

        JLabel title = new JLabel("  Pull Requests", Icons.PR, SwingConstants.LEFT);
        title.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 11f));
        header.add(title, BorderLayout.WEST);

        JButton createBtn = new JButton(Icons.PLUS);
        createBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        createBtn.setToolTipText("New Pull Request");
        header.add(createBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // --- 2. PR List ---
        prList = new JPanel();
        prList.setLayout(new BoxLayout(prList, BoxLayout.Y_AXIS));
        prList.setBackground(UIManager.getColor("TextArea.background"));
        
        addPRItem("#42: Refactor Renderer API", "batista", "2 hours ago");
        addPRItem("#41: Add Vulkan backend support", "parafield", "Yesterday");
        addPRItem("#40: Fix crash on window resize", "contributor1", "3 days ago");

        JScrollPane scrollPane = new JScrollPane(prList);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. Footer ---
        JButton createPRBigBtn = new JButton("Create Pull Request...");
        createPRBigBtn.putClientProperty(FlatClientProperties.STYLE, "background: #27ae60; foreground: #ffffff;");
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(new EmptyBorder(10, 10, 10, 10));
        footer.add(createPRBigBtn, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void addPRItem(String title, String author, String time) {
        JPanel item = new JPanel(new GridBagLayout());
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        item.setPreferredSize(new Dimension(0, 50));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 10, 0, 10);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 12f));
        gbc.gridy = 0;
        item.add(titleLabel, gbc);

        JLabel infoLabel = new JLabel(author + " • " + time);
        infoLabel.setFont(UIManager.getFont("defaultFont").deriveFont(10f));
        infoLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 5, 10);
        item.add(infoLabel, gbc);
        
        prList.add(item);
    }
}

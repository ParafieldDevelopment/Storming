package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import java.awt.*;

public class HierarchyPanel extends JPanel {
    public HierarchyPanel() {
        setLayout(new BorderLayout());
        
        // Search bar
        JPanel searchBox = new JPanel(new BorderLayout());
        searchBox.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
        searchBox.setPreferredSize(new Dimension(0, 32));
        
        JTextField searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search...");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Icons.SEARCH);
        searchField.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 2%); borderWidth: 0; focusWidth: 0;");
        searchBox.add(searchField, BorderLayout.CENTER);
        
        add(searchBox, BorderLayout.NORTH);

        JTree tree = new JTree();
        tree.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 1%)");
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
        
        int size = 80;
        int x = getWidth() - size - 25;
        int y = getHeight() - size - 25;
        if (Icons.FOLDER_80 != null) {
            Icons.FOLDER_80.paintIcon(this, g2, x, y);
        }
        g2.dispose();
    }
}

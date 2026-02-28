package com.parafield.storming.ui.panels;

import com.parafield.storming.Icons;
import javax.swing.*;
import java.awt.*;

public class HierarchyPanel extends JPanel {
    public HierarchyPanel() {
        setLayout(new BorderLayout());
        JTree tree = new JTree();
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

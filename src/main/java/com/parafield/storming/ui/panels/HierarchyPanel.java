package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.parafield.storming.ui.windows.MainWindow;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.List;

/**
 * Displays the hierarchical structure of the current scene.
 * Dynamically updates to reflect the entities existing in the C++ engine.
 */
public class HierarchyPanel extends JPanel {

    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;

    /**
     * Constructs a HierarchyPanel, initializing the search bar and the object tree.
     */
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

        rootNode = new DefaultMutableTreeNode("Scene (Active)");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 1%)");
        tree.setRowHeight(24);
        tree.setShowsRootHandles(true);
        
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode != null && selectedNode.getUserObject() instanceof EntityItem item) {
                MainWindow.getInstance().onEntitySelected(item);
            }
        });
        
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    /**
     * Updates the hierarchy tree with a new list of entities.
     * @param entities List of EntityItem objects to display.
     */
    public void updateHierarchyFromItems(List<EntityItem> entities) {
        SwingUtilities.invokeLater(() -> {
            rootNode.removeAllChildren();
            for (EntityItem entity : entities) {
                rootNode.add(new DefaultMutableTreeNode(entity));
            }
            treeModel.reload();
            for (int i = 0; i < tree.getRowCount(); i++) {
                tree.expandRow(i);
            }
        });
    }

    public record EntityItem(int id, String name) {
        @Override
        public String toString() {
            return name;
        }
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

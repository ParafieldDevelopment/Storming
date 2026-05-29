package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;

/**
 * Provides a file system explorer for the current project.
 * Displays a hierarchical tree of files and directories with custom icons and refreshing capabilities.
 */
public class ProjectBrowserPanel extends JPanel {

    private final JTree fileTree;
    private final File rootDir;

    /**
     * Constructs a ProjectBrowserPanel, initializing the file tree with the specified directory.
     * @param root The project root directory to browse.
     */
    public ProjectBrowserPanel(File root) {
        this.rootDir = root;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 8, 8, 8));
        
        // --- 1. Toolbar ---
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        refreshBtn.addActionListener(e -> refreshTree());
        
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(refreshBtn);
        toolbar.addSeparator();
        
        JLabel pathLabel = new JLabel(rootDir.getAbsolutePath());
        pathLabel.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 11f));
        pathLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(pathLabel);
        
        add(toolbar, BorderLayout.NORTH);

        // --- 2. File Tree ---
        DefaultMutableTreeNode rootNode = createTreeNodes(rootDir);
        fileTree = new JTree(new DefaultTreeModel(rootNode));
        fileTree.setCellRenderer(new FileTreeCellRenderer());
        fileTree.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 2%)");
        fileTree.setRowHeight(24);
        fileTree.setShowsRootHandles(true);
        
        fileTree.setDragEnabled(true);
        fileTree.setTransferHandler(new TransferHandler() {
            @Override
            protected java.awt.datatransfer.Transferable createTransferable(JComponent c) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                if (node != null && node.getUserObject() instanceof File file) {
                    return new java.awt.datatransfer.StringSelection(file.getAbsolutePath());
                }
                return null;
            }
            @Override public int getSourceActions(JComponent c) { return COPY; }
        });

        JScrollPane scrollPane = new JScrollPane(fileTree);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Refreshes the file tree by re-scanning the root directory.
     */
    private void refreshTree() {
        DefaultMutableTreeNode rootNode = createTreeNodes(rootDir);
        fileTree.setModel(new DefaultTreeModel(rootNode));
    }

    /**
     * Recursively creates tree nodes for a given file or directory.
     * @param file The root file or directory to scan.
     * @return A DefaultMutableTreeNode representing the file/directory and its children.
     */
    private DefaultMutableTreeNode createTreeNodes(File file) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(file); // Store File object directly
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                // Sort files: directories first
                java.util.Arrays.sort(files, (f1, f2) -> {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return f1.getName().compareToIgnoreCase(f2.getName());
                });
                for (File child : files) {
                    if (child.getName().startsWith(".")) continue; // Skip hidden files
                    node.add(createTreeNodes(child));
                }
            }
        }
        return node;
    }

    /** Custom tree cell renderer to provide distinct icons for files and folders. */
    private static class FileTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
            Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObj instanceof File file) {
                setText(file.getName().isEmpty() ? file.getPath() : file.getName());
                if (file.isDirectory()) {
                    setIcon(Icons.FOLDER);
                } else {
                    setIcon(UIManager.getIcon("FileView.fileIcon"));
                }
            }
            return this;
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

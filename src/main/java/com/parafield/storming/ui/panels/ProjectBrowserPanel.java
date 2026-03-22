package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;

/**
 * Provides a file system explorer for the current project.
 */
public class ProjectBrowserPanel extends JPanel {

    private final JTree fileTree;
    private final File rootDir;

    public ProjectBrowserPanel(File root) {
        this.rootDir = root;
        setLayout(new BorderLayout());
        
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

        fileTree.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { if (e.isPopupTrigger()) showPopup(e); }
            @Override public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) showPopup(e); }
        });

        JScrollPane scrollPane = new JScrollPane(fileTree);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void showPopup(MouseEvent e) {
        int row = fileTree.getClosestRowForLocation(e.getX(), e.getY());
        if (row != -1) fileTree.setSelectionRow(row);
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        File file = (node != null) ? (File) node.getUserObject() : rootDir;
        File targetDir = file.isDirectory() ? file : file.getParentFile();

        JPopupMenu menu = new JPopupMenu();
        JMenu newMenu = new JMenu("New");
        newMenu.add(new JMenuItem("Normal Script (.lua)")).addActionListener(ev -> createScript(targetDir, "Normal"));
        newMenu.add(new JMenuItem("Module Script (.lua)")).addActionListener(ev -> createScript(targetDir, "Module"));
        menu.add(newMenu);
        menu.addSeparator();
        menu.add(new JMenuItem("Refresh")).addActionListener(ev -> refreshTree());
        
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void createScript(File dir, String type) {
        String name = JOptionPane.showInputDialog(this, "Enter script name:", "New " + type + " Script", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;
        if (!name.endsWith(".lua")) name += ".lua";

        File newFile = new File(dir, name);
        try {
            String content = type.equals("Normal") ? 
                "-- Normal Script Template\n\nfunction OnCreate(self)\n    Log(\"Entity Created!\")\nend\n\nfunction OnUpdate(self, dt)\n    -- Logic here\nend\n" :
                "-- Module Script Template\nlocal M = {}\n\nfunction M.greet()\n    Log(\"Hello from Module!\")\nend\n\nreturn M\n";
            Files.writeString(newFile.toPath(), content);
            refreshTree();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to create script: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTree() {
        DefaultMutableTreeNode rootNode = createTreeNodes(rootDir);
        fileTree.setModel(new DefaultTreeModel(rootNode));
    }

    private final java.util.Set<String> HIDDEN_FOLDERS = java.util.Set.of(
        "src", "Engine", "gradle", ".gradle", ".idea", ".git", ".github", "build", 
        "dependences", "packaging", "bin", "obj", "target", ".settings"
    );

    private DefaultMutableTreeNode createTreeNodes(File file) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                java.util.Arrays.sort(files, (f1, f2) -> {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return f1.getName().compareToIgnoreCase(f2.getName());
                });
                for (File child : files) {
                    if (child.getName().startsWith(".")) continue;
                    // HIDE SOURCE AND BUILD FOLDERS
                    if (HIDDEN_FOLDERS.contains(child.getName())) continue;
                    
                    node.add(createTreeNodes(child));
                }
            }
        }
        return node;
    }

    private static class FileTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
            Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObj instanceof File file) {
                setText(file.getName().isEmpty() ? file.getPath() : file.getName());
                if (file.isDirectory()) setIcon(Icons.FOLDER);
                else setIcon(UIManager.getIcon("FileView.fileIcon"));
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
        int size = 80; int x = getWidth() - size - 25; int y = getHeight() - size - 25;
        if (Icons.FOLDER_80 != null) Icons.FOLDER_80.paintIcon(this, g2, x, y);
        g2.dispose();
    }
}

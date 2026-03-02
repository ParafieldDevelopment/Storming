package com.parafield.storming.ui.windows;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.parafield.storming.ui.utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * An enhanced modal dialog for creating a new Storming Engine project.
 * Features template selection, real-time path validation, and a modern layout.
 */
public class NewProjectDialog extends JDialog {

    private final JTextField nameField;
    private final JTextField pathField;
    private final JLabel previewLabel;
    private final JList<TemplateItem> templateList;
    private boolean result = false;

    public NewProjectDialog(Frame owner) {
        super(owner, "Create New Project", true);
        setSize(750, 500);
        setLocationRelativeTo(owner);
        
        rootPane.putClientProperty("apple.awt.fullWindowContent", true);
        rootPane.putClientProperty("apple.awt.transparentTitleBar", true);

        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // --- 1. Left Sidebar (Templates) ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));
        sidebar.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 2%)");

        JLabel templateHeader = new JLabel("Templates");
        templateHeader.setFont(UIUtils.getFont(Font.BOLD, 14f));
        templateHeader.setBorder(new EmptyBorder(35, 20, 15, 20));
        sidebar.add(templateHeader, BorderLayout.NORTH);

        DefaultListModel<TemplateItem> model = new DefaultListModel<>();
        model.addElement(new TemplateItem("Empty 2D", "A clean start for your new game.", Icons.PLUS));
        model.addElement(new TemplateItem("2D Demo", "Includes physics and sprite examples.", Icons.EXE_ICON));
        
        templateList = new JList<>(model);
        templateList.setCellRenderer(new TemplateRenderer());
        templateList.setFixedCellHeight(60);
        templateList.setOpaque(false);
        templateList.setSelectedIndex(0);
        
        sidebar.add(templateList, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // --- 2. Main Content (Project Configuration) ---
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(35, 30, 20, 30));

        JLabel titleLabel = new JLabel("Project Configuration");
        titleLabel.setFont(UIUtils.getFont(Font.BOLD, 20f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLabel);
        
        content.add(Box.createVerticalStrut(25));

        // Name Field
        content.add(createLabel("Project Name"));
        nameField = new JTextField("MyNewProject");
        nameField.setPreferredSize(new Dimension(0, 35));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(nameField);

        content.add(Box.createVerticalStrut(20));

        // Path Field
        content.add(createLabel("Project Location"));
        JPanel pathPanel = new JPanel(new BorderLayout(10, 0));
        pathPanel.setOpaque(false);
        pathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pathPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        pathField = new JTextField(System.getProperty("user.home") + File.separator + "StormingProjects");
        pathPanel.add(pathField, BorderLayout.CENTER);

        JButton browseBtn = new JButton(Icons.FOLDER);
        browseBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                pathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        pathPanel.add(browseBtn, BorderLayout.EAST);
        content.add(pathPanel);

        content.add(Box.createVerticalStrut(15));

        // Preview Area
        previewLabel = new JLabel();
        previewLabel.setFont(UIUtils.getFont(Font.ITALIC, 11f));
        previewLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        previewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(previewLabel);

        root.add(content, BorderLayout.CENTER);

        // --- 3. Footer ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton createBtn = new JButton("Create Project");
        createBtn.putClientProperty(FlatClientProperties.STYLE, "background: #3498db; foreground: #ffffff;");
        createBtn.setPreferredSize(new Dimension(140, 35));
        createBtn.addActionListener(e -> handleCreate());

        footer.add(cancelBtn);
        footer.add(createBtn);
        root.add(footer, BorderLayout.SOUTH);

        // --- Listeners ---
        DocumentListener updatePreview = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePreview(); }
            public void removeUpdate(DocumentEvent e) { updatePreview(); }
            public void changedUpdate(DocumentEvent e) { updatePreview(); }
        };
        nameField.getDocument().addDocumentListener(updatePreview);
        pathField.getDocument().addDocumentListener(updatePreview);
        updatePreview();
    }

    private void updatePreview() {
        String name = nameField.getText().trim();
        String path = pathField.getText().trim();
        if (name.isEmpty()) {
            previewLabel.setText("Please enter a project name.");
            previewLabel.setForeground(new Color(231, 76, 60));
        } else {
            previewLabel.setText("Project will be created at: " + path + File.separator + name);
            previewLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        }
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIUtils.getFont(Font.BOLD, 12f));
        l.setBorder(new EmptyBorder(0, 0, 5, 0));
        return l;
    }

    private void handleCreate() {
        String name = nameField.getText().trim();
        String pathStr = pathField.getText().trim();

        if (name.isEmpty() || pathStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Path projectRoot = Paths.get(pathStr, name);
            if (Files.exists(projectRoot)) {
                JOptionPane.showMessageDialog(this, "Directory already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Files.createDirectories(projectRoot);
            Files.createDirectories(projectRoot.resolve("assets"));
            Files.createDirectories(projectRoot.resolve("scenes"));

            String templateName = templateList.getSelectedValue().name;
            String stormContent = "{\n" +
                    "  \"name\": \"" + name + "\",\n" +
                    "  \"version\": \"1.0.0\",\n" +
                    "  \"template\": \"" + templateName + "\",\n" +
                    "  \"main_scene\": \"scenes/main.storm_scene\"\n" +
                    "}";
            
            Files.writeString(projectRoot.resolve(name + ".storm"), stormContent);

            // Create default main scene
            String sceneContent = "{\n" +
                    "  \"entities\": [\n" +
                    "    { \"name\": \"Main Camera\", \"components\": { \"Transform\": {} } },\n" +
                    "    { \"name\": \"Directional Light\", \"components\": { \"Transform\": {} } }\n" +
                    "  ]\n" +
                    "}";
            Files.writeString(projectRoot.resolve("scenes/main.storm_scene"), sceneContent);

            result = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to create project: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccessful() { return result; }

    /**
     * Returns the full path to the newly created project directory.
     * @return The project directory path.
     */
    public String getProjectPath() {
        return Paths.get(pathField.getText().trim(), nameField.getText().trim()).toString();
    }

    private record TemplateItem(String name, String desc, Icon icon) {}

    private static class TemplateRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            TemplateItem item = (TemplateItem) value;
            JPanel p = new JPanel(new BorderLayout(15, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    if (isSelected) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(UIManager.getColor("List.selectionBackground"));
                        g2.fillRoundRect(10, 2, getWidth() - 20, getHeight() - 4, 8, 8);
                        g2.dispose();
                    }
                }
            };
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(10, 25, 10, 25));
            
            JLabel iconLabel = new JLabel(item.icon);
            
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            
            JLabel nameLabel = new JLabel(item.name);
            nameLabel.setFont(UIUtils.getFont(Font.BOLD, 13f));
            nameLabel.setForeground(isSelected ? UIManager.getColor("List.selectionForeground") : UIManager.getColor("Label.foreground"));
            
            JLabel descLabel = new JLabel(item.desc);
            descLabel.setFont(UIUtils.getFont(Font.PLAIN, 10f));
            descLabel.setForeground(isSelected ? UIManager.getColor("List.selectionForeground") : UIManager.getColor("Label.disabledForeground"));
            
            textPanel.add(nameLabel);
            textPanel.add(descLabel);
            
            p.add(iconLabel, BorderLayout.WEST);
            p.add(textPanel, BorderLayout.CENTER);
            return p;
        }
    }
}

package com.parafield.storming.ui.windows;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.parafield.storming.Icons;
import com.parafield.storming.ui.utils.UIAnimator;
import com.parafield.storming.ui.utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * The Settings window for the Storming Engine Editor.
 * Provides a categorized interface for application and engine configuration,
 * featuring a searchable category list and dynamic content panels with smooth transitions.
 */
public class SettingsWindow extends JDialog {

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final JList<CategoryItem> categoryList;
    private final JLabel headerLabel;
    
    private float contentAlpha = 0.0f;
    private int contentYOffset = 20;

    /**
     * Constructs a SettingsWindow.
     * @param owner The parent frame for this dialog.
     */
    public SettingsWindow(Window owner) {
        super(owner, "Settings", ModalityType.APPLICATION_MODAL);
        setSize(900, 650);
        setLocationRelativeTo(owner);
        
        rootPane.putClientProperty("apple.awt.fullWindowContent", true);
        rootPane.putClientProperty("apple.awt.transparentTitleBar", true);

        setLayout(new BorderLayout());

        // --- 1. Sidebar (Search + Categories) ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));
        sidebar.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 2%)");

        // Search Field
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(35, 15, 10, 15));
        
        JTextField searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search settings...");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Icons.SEARCH);
        searchField.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: darken($Panel.background, 4%); borderWidth: 0; focusWidth: 0;");
        searchPanel.add(searchField, BorderLayout.CENTER);
        sidebar.add(searchPanel, BorderLayout.NORTH);

        DefaultListModel<CategoryItem> model = new DefaultListModel<>();
        model.addElement(new CategoryItem("Appearance", Icons.BRUSH));
        model.addElement(new CategoryItem("Engine", Icons.LOGO_80)); 
        model.addElement(new CategoryItem("Editor", Icons.SETTINGS));
        model.addElement(new CategoryItem("Git", Icons.GIT));
        model.addElement(new CategoryItem("Terminal", Icons.CONSOLE));

        categoryList = new JList<>(model);
        categoryList.setCellRenderer(new CategoryRenderer());
        categoryList.setFixedCellHeight(36);
        categoryList.setOpaque(false);
        categoryList.setSelectedIndex(0);
        categoryList.setBorder(new EmptyBorder(0, 8, 0, 8));

        JScrollPane scrollPane = new JScrollPane(categoryList);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        sidebar.add(scrollPane, BorderLayout.CENTER);

        // --- 2. Content Area ---
        JPanel rightPanel = new JPanel(new BorderLayout()) {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, contentAlpha));
                g2.translate(0, contentYOffset);
                super.paint(g2);
                g2.dispose();
            }
        };
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
        header.setOpaque(false);
        
        headerLabel = new JLabel("Appearance");
        headerLabel.setFont(UIUtils.getFont(Font.BOLD, 20f));
        headerLabel.setBorder(new EmptyBorder(25, 30, 0, 0));
        header.add(headerLabel, BorderLayout.WEST);
        rightPanel.add(header, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        contentPanel.setOpaque(false);

        contentPanel.add(createAppearancePanel(), "Appearance");
        contentPanel.add(createEnginePanel(), "Engine");
        contentPanel.add(createEditorPanel(), "Editor");
        contentPanel.add(createPlaceholderPanel("Version Control (Git)"), "Git");
        contentPanel.add(createPlaceholderPanel("Terminal Emulator Settings"), "Terminal");

        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                CategoryItem selected = categoryList.getSelectedValue();
                if (selected != null) {
                    animateCategoryTransition(selected.name);
                }
            }
        });

        rightPanel.add(contentPanel, BorderLayout.CENTER);

        // --- 3. Footer ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        
        JButton applyBtn = new JButton("Apply");
        applyBtn.setEnabled(false);
        
        JButton okBtn = new JButton("OK");
        okBtn.putClientProperty(FlatClientProperties.STYLE, "background: #3498db; foreground: #ffffff;");
        okBtn.addActionListener(e -> dispose());

        footer.add(cancelBtn);
        footer.add(applyBtn);
        footer.add(okBtn);

        add(sidebar, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        
        // Start entrance animation
        SwingUtilities.invokeLater(this::animateEntrance);
    }

    /**
     * Performs an entrance animation for the content area.
     */
    private void animateEntrance() {
        UIAnimator.animate(0.0f, 1.0f, 500, a -> {
            contentAlpha = a;
            contentYOffset = (int)(20 * (1.0f - a));
            repaint();
        }, null);
    }

    /**
     * Animates the transition between setting categories.
     * @param targetName The name of the category to transition to.
     */
    private void animateCategoryTransition(String targetName) {
        // Fade out current
        UIAnimator.animate(1.0f, 0.0f, 150, a -> {
            contentAlpha = a;
            repaint();
        }, () -> {
            // Switch card
            cardLayout.show(contentPanel, targetName);
            headerLabel.setText(targetName);
            // Fade in new
            UIAnimator.animate(0.0f, 1.0f, 200, a -> {
                contentAlpha = a;
                repaint();
            }, null);
        });
    }

    /**
     * Creates the panel for Editor-specific preferences.
     * @return A JPanel containing autosave, keymap, and editor behavior settings.
     */
    private JPanel createEditorPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        p.add(createSectionHeader("General"));
        p.add(Box.createVerticalStrut(10));
        
        JCheckBox autoSave = new JCheckBox("Autosave files on window focus loss");
        autoSave.setSelected(true);
        autoSave.setOpaque(false);
        p.add(createSettingRow("Autosave:", autoSave));
        
        p.add(Box.createVerticalStrut(8));
        p.add(createSettingRow("Keymap:", new JComboBox<>(new String[]{"Default", "IntelliJ IDEA", "Visual Studio", "Sublime Text"})));
        
        p.add(Box.createVerticalStrut(30));

        p.add(createSectionHeader("Code Style"));
        p.add(Box.createVerticalStrut(10));
        
        p.add(createSettingRow("Tab Size:", new JSpinner(new SpinnerNumberModel(4, 1, 8, 1))));
        p.add(Box.createVerticalStrut(8));
        
        JCheckBox useSpaces = new JCheckBox("Insert spaces instead of tabs");
        useSpaces.setSelected(true);
        useSpaces.setOpaque(false);
        p.add(createSettingRow("Indentation:", useSpaces));

        p.add(Box.createVerticalGlue());
        return p;
    }

    /**
     * Creates the panel for Engine-specific settings.
     * @return A JPanel containing engine path, graphics API, and logging configuration.
     */
    private JPanel createEnginePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        // Core Engine Section
        p.add(createSectionHeader("Runtime Environment"));
        p.add(Box.createVerticalStrut(10));
        
        JPanel pathPanel = new JPanel(new BorderLayout(5, 0));
        pathPanel.setOpaque(false);
        JTextField pathField = new JTextField("Engine/2D/build/bin/StormingEngine");
        JButton browseBtn = new JButton("Browse...");
        pathPanel.add(pathField, BorderLayout.CENTER);
        pathPanel.add(browseBtn, BorderLayout.EAST);
        
        p.add(createSettingRow("Engine Path:", pathPanel));
        p.add(Box.createVerticalStrut(8));
        
        p.add(createSettingRow("Graphics API:", new JComboBox<>(new String[]{"OpenGL 4.5 Core (Default)", "Vulkan 1.3 (Experimental)", "DirectX 12 (Windows Only)"})));
        p.add(Box.createVerticalStrut(8));
        
        p.add(createSettingRow("Log Level:", new JComboBox<>(new String[]{"Debug", "Info", "Warning", "Error", "Critical"})));
        
        p.add(Box.createVerticalStrut(30));

        // Shared Memory Section
        p.add(createSectionHeader("Performance & IPC"));
        p.add(Box.createVerticalStrut(10));
        
        JCheckBox shmCheck = new JCheckBox("Use Shared Memory for Real-time Streaming");
        shmCheck.setSelected(true);
        shmCheck.setOpaque(false);
        p.add(createSettingRow("Streaming:", shmCheck));
        
        p.add(Box.createVerticalStrut(8));
        p.add(createSettingRow("Buffer Size:", new JComboBox<>(new String[]{"Double Buffered", "Triple Buffered"})));

        p.add(Box.createVerticalGlue());
        return p;
    }

    /**
     * Creates the panel for Appearance settings.
     * @return A JPanel containing appearance-related configuration components.
     */
    private JPanel createAppearancePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        // UI Theme Section
        p.add(createSectionHeader("User Interface"));
        p.add(Box.createVerticalStrut(10));
        
        String[] themes = {"Mac Dark", "Mac Light", "Flat Dark", "Flat Light"};
        JComboBox<String> themeCombo = new JComboBox<>(themes);
        
        // Set initial selection based on current Look and Feel
        String currentLaf = UIManager.getLookAndFeel().getClass().getSimpleName();
        if (currentLaf.contains("MacDark")) themeCombo.setSelectedIndex(0);
        else if (currentLaf.contains("MacLight")) themeCombo.setSelectedIndex(1);
        else if (currentLaf.contains("FlatDark")) themeCombo.setSelectedIndex(2);
        else if (currentLaf.contains("FlatLight")) themeCombo.setSelectedIndex(3);

        themeCombo.addActionListener(e -> {
            String selected = (String) themeCombo.getSelectedItem();
            try {
                if ("Mac Dark".equals(selected)) UIManager.setLookAndFeel(new FlatMacDarkLaf());
                else if ("Mac Light".equals(selected)) UIManager.setLookAndFeel(new FlatMacLightLaf());
                else if ("Flat Dark".equals(selected)) UIManager.setLookAndFeel(new FlatDarkLaf());
                else if ("Flat Light".equals(selected)) UIManager.setLookAndFeel(new FlatLightLaf());
                
                FlatLaf.updateUI();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        p.add(createSettingRow("Theme:", themeCombo));
        p.add(Box.createVerticalStrut(8));
        p.add(createSettingRow("Zoom Level:", new JComboBox<>(new String[]{"100%", "125%", "150%", "200%"})));
        
        p.add(Box.createVerticalStrut(30));

        // Editor Section
        p.add(createSectionHeader("Editor Font"));
        p.add(Box.createVerticalStrut(10));
        
        JPanel fontControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fontControls.setOpaque(false);
        JComboBox<String> fontCombo = new JComboBox<>(new String[]{"JetBrains Mono", "Fira Code", "Consolas"});
        fontCombo.setPreferredSize(new Dimension(200, 30));
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(13, 8, 72, 1));
        sizeSpinner.setPreferredSize(new Dimension(65, 30));
        fontControls.add(fontCombo);
        fontControls.add(Box.createHorizontalStrut(10));
        fontControls.add(sizeSpinner);
        
        p.add(createSettingRow("Font Family:", fontControls));
        p.add(Box.createVerticalStrut(8));
        p.add(createSettingRow("Line Spacing:", new JSpinner(new SpinnerNumberModel(1.2, 0.5, 3.0, 0.1))));

        p.add(Box.createVerticalGlue());
        return p;
    }

    /**
     * Creates a standardized row for a setting.
     * @param label The name of the setting.
     * @param component The interactive component for the setting.
     * @return A JPanel representing one setting row.
     */
    private JPanel createSettingRow(String label, JComponent component) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(600, 30));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(120, 30));
        l.setFont(UIUtils.getFont(Font.PLAIN, 12f));
        
        component.setMaximumSize(new Dimension(300, 30));
        
        row.add(l, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        return row;
    }

    /**
     * Creates a stylized header for a setting section.
     * @param text The header text.
     * @return A JLabel configured as a section header.
     */
    private JLabel createSectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIUtils.getFont(Font.BOLD, 14f));
        l.setForeground(UIManager.getColor("Label.disabledForeground"));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    /**
     * Creates a placeholder panel for categories that are not yet fully implemented.
     * @param text The text to display in the placeholder.
     * @return A JPanel with a centered placeholder label.
     */
    private JPanel createPlaceholderPanel(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel("<html><center>" + text + "<br><font size='3' color='#888888'>Configure " + text.toLowerCase() + " options here.</font></center></html>", SwingConstants.CENTER);
        l.setFont(UIUtils.getFont(Font.PLAIN, 15f));
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    /** Represents a category entry in the settings sidebar. */
    private record CategoryItem(String name, Icon icon) {}

    /** Custom renderer for settings categories with JetBrains-style selection. */
    private static class CategoryRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            CategoryItem item = (CategoryItem) value;
            JPanel p = new JPanel(new BorderLayout(10, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    if (isSelected) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(UIManager.getColor("List.selectionBackground"));
                        g2.fillRoundRect(0, 2, getWidth(), getHeight() - 4, 6, 6);
                        g2.dispose();
                    }
                }
            };
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(0, 10, 0, 10));
            
            JLabel iconLabel = new JLabel(item.icon);
            JLabel textLabel = new JLabel(item.name);
            textLabel.setFont(UIUtils.getFont(isSelected ? Font.BOLD : Font.PLAIN, 13f));
            textLabel.setForeground(isSelected ? UIManager.getColor("List.selectionForeground") : UIManager.getColor("Label.foreground"));
            
            p.add(iconLabel, BorderLayout.WEST);
            p.add(textLabel, BorderLayout.CENTER);
            return p;
        }
    }
}

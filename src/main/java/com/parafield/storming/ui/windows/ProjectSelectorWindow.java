package com.parafield.storming.ui.windows;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.parafield.storming.core.ProjectManager;
import com.parafield.storming.ui.utils.UIAnimator;
import com.parafield.storming.ui.utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * The initial launcher window for the Storming Engine.
 * Allows users to select existing projects or create new ones.
 * Features a modern, animated interface with a sidebar for project history.
 */
public class ProjectSelectorWindow extends JFrame {
    private AnimatedPanel mainContent;
    private JPanel sidebar;
    private DefaultListModel<ProjectItem> listModel;
    private CardLayout sidebarCardLayout;
    private JPanel sidebarCardPanel;

    /**
     * Constructs the ProjectSelectorWindow and sets up its UI properties.
     */
    public ProjectSelectorWindow() {
        setTitle("Storming Engine Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        
        rootPane.putClientProperty("apple.awt.fullWindowContent", true);
        rootPane.putClientProperty("apple.awt.transparentTitleBar", true);
        rootPane.putClientProperty("flatlaf.showWindowIcon", false);

        initUI();
    }

    /**
     * Initializes the UI components, including the project list sidebar and the welcome/action area.
     */
    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // --- LEFT SIDEBAR ---
        sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(300, 0));
        sidebar.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 2%)");
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));

        JLabel titleLabel = new JLabel("Projects");
        titleLabel.setFont(UIUtils.getFont(Font.BOLD, 16f));
        titleLabel.setBorder(new EmptyBorder(30, 25, 15, 25));
        sidebar.add(titleLabel, BorderLayout.NORTH);

        // Project List
        listModel = new DefaultListModel<>();
        
        JList<ProjectItem> projectList = new JList<>(listModel);
        ProjectListRenderer renderer = new ProjectListRenderer();
        projectList.setCellRenderer(renderer);
        projectList.setOpaque(false);
        projectList.setFixedCellHeight(60);
        projectList.setBorder(new EmptyBorder(0, 5, 0, 5));
        
        // Track hover state
        projectList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int index = projectList.locationToIndex(e.getPoint());
                if (index != renderer.hoverIndex) {
                    renderer.hoverIndex = index;
                    projectList.repaint();
                }
            }
        });
        projectList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                renderer.hoverIndex = -1;
                projectList.repaint();
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ProjectItem selected = projectList.getSelectedValue();
                    if (selected != null) {
                        launchMainEditor(selected.path);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(projectList);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Empty State Panel
        JPanel emptyState = new JPanel(new GridBagLayout());
        emptyState.setOpaque(false);
        GridBagConstraints gbcEmpty = new GridBagConstraints();
        gbcEmpty.gridx = 0; gbcEmpty.gridy = 0;
        
        JLabel img404 = new JLabel(Icons.NOT_FOUND);
        emptyState.add(img404, gbcEmpty);
        
        gbcEmpty.gridy++;
        gbcEmpty.insets = new Insets(15, 0, 0, 0);
        JLabel emptyMsg = new JLabel("<html><center>No projects found.<br>Create one to get started!</center></html>");
        emptyMsg.setFont(UIUtils.getFont(Font.PLAIN, 12f));
        emptyMsg.setForeground(UIManager.getColor("Label.disabledForeground"));
        emptyState.add(emptyMsg, gbcEmpty);

        sidebarCardLayout = new CardLayout();
        sidebarCardPanel = new JPanel(sidebarCardLayout);
        sidebarCardPanel.setOpaque(false);
        sidebarCardPanel.add(scrollPane, "LIST");
        sidebarCardPanel.add(emptyState, "EMPTY");

        sidebar.add(sidebarCardPanel, BorderLayout.CENTER);

        refreshProjectList();

        // Sidebar Footer
        JPanel sidebarFooter = new JPanel(new BorderLayout());
        sidebarFooter.setOpaque(false);
        sidebarFooter.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        JButton settingsBtn = new JButton(" Settings", Icons.SETTINGS);
        settingsBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        settingsBtn.addActionListener(e -> new SettingsWindow(this).setVisible(true));
        sidebarFooter.add(settingsBtn, BorderLayout.WEST);
        
        sidebar.add(sidebarFooter, BorderLayout.SOUTH);
        root.add(sidebar, BorderLayout.WEST);

        // --- MAIN CONTENT (Welcome Screen with Animation) ---
        mainContent = new AnimatedPanel(new GridBagLayout());
        mainContent.setBackground(UIManager.getColor("Panel.background"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);

        // Big Logo
        JLabel logoLabel = new JLabel(Icons.LOGO);
        mainContent.add(logoLabel, gbc);

        // Title
        gbc.gridy++;
        JLabel welcomeTitle = new JLabel("Storming Engine");
        welcomeTitle.setFont(UIUtils.getFont(Font.BOLD, 32f));
        mainContent.add(welcomeTitle, gbc);

        // Subtitle
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 40, 0);
        JLabel subtitle = new JLabel("High Performance 2D/3D Game Development");
        subtitle.setForeground(UIManager.getColor("Label.disabledForeground"));
        mainContent.add(subtitle, gbc);

        // Action Buttons
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actions.setOpaque(false);

        JButton newProjectBtn = createActionButton("New Project", "#3498db", true);
        newProjectBtn.addActionListener(e -> {
            NewProjectDialog dialog = new NewProjectDialog(this);
            dialog.setVisible(true);
            if (dialog.isSuccessful()) {
                launchMainEditor(dialog.getProjectPath());
            }
        });
        
        JButton openProjectBtn = createActionButton("Open Project", null, false);
        openProjectBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setDialogTitle("Select Storming Project");
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selected = chooser.getSelectedFile();
                String path = selected.isDirectory() ? selected.getAbsolutePath() : selected.getParent();
                launchMainEditor(path);
            }
        });
        
        actions.add(newProjectBtn);
        actions.add(openProjectBtn);
        mainContent.add(actions, gbc);

        // Version Info
        gbc.gridy++;
        gbc.insets = new Insets(50, 0, 0, 0);
        JLabel versionLabel = new JLabel("v2026.1 Alpha Preview");
        versionLabel.setFont(UIUtils.getFont(Font.PLAIN, 11f));
        versionLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        mainContent.add(versionLabel, gbc);

        root.add(mainContent, BorderLayout.CENTER);
        
        // Start animation after layout is ready
        SwingUtilities.invokeLater(() -> mainContent.startEntrance());
    }

    /**
     * Internal panel that supports alpha-fade and slide-up entrance animations.
     */
    private static class AnimatedPanel extends JPanel {
        float alpha = 0.0f;
        int yOffset = 30; 

        public AnimatedPanel(LayoutManager layout) {
            super(layout);
        }

        /**
         * Starts the entrance animation.
         */
        public void startEntrance() {
            UIAnimator.animate(0.0f, 1.0f, 600, a -> {
                alpha = Math.max(0.0f, Math.min(1.0f, a));
                yOffset = (int)(30 * (1.0f - alpha));
                repaint();
            }, null);
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.translate(0, yOffset);
            super.paint(g2);
            g2.dispose();
        }
    }

    /**
     * Utility method to create a stylized action button.
     * @param text The button text.
     * @param colorHex Optional background color in hex.
     * @param primary Whether this is a primary (highlighted) action.
     * @return A configured JButton.
     */
    private JButton createActionButton(String text, String colorHex, boolean primary) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(180, 45));
        btn.setFont(UIUtils.getFont(Font.BOLD, 14f));
        
        String style = "arc: 10;";
        if (primary && colorHex != null) {
            style += "background: " + colorHex + "; foreground: #ffffff; borderWidth: 0;";
        }
        btn.putClientProperty(FlatClientProperties.STYLE, style);
        return btn;
    }

    private void refreshProjectList() {
        listModel.clear();
        List<ProjectManager.ProjectEntry> recents = ProjectManager.getRecentProjects();
        for (ProjectManager.ProjectEntry entry : recents) {
            listModel.addElement(new ProjectItem(entry.name(), entry.path()));
        }

        if (listModel.isEmpty()) {
            sidebarCardLayout.show(sidebarCardPanel, "EMPTY");
        } else {
            sidebarCardLayout.show(sidebarCardPanel, "LIST");
        }
    }

    /**
     * Launches the main editor window with an exit animation.
     * @param projectPath The path to the project to open.
     */
    private void launchMainEditor(String projectPath) {
        ProjectManager.addRecentProject(projectPath);
        animateExit(() -> {
            dispose();
            new MainWindow(projectPath).setVisible(true);
        });
    }

    /**
     * Performs an exit animation for both the sidebar and the main content area.
     * @param onComplete Callback to execute once the animation finishes.
     */
    private void animateExit(Runnable onComplete) {
        // Slide sidebar left and fade
        UIAnimator.animate(0, -350, 350, x -> {
            sidebar.setMinimumSize(new Dimension(0, 0));
            sidebar.setPreferredSize(new Dimension((int)Math.max(0, 300 + x), 0));
            sidebar.revalidate();
        }, null);

        // Slide main content right and fade
        UIAnimator.animate(1.0f, 0.0f, 350, alpha -> {
            mainContent.alpha = alpha;
            mainContent.yOffset = (int)(80 * (1.0f - alpha)); // Larger slide distance
            mainContent.repaint();
        }, onComplete);
    }

    /** Represents a project entry in the sidebar list. */
    private static class ProjectItem {
        String name, path;
        ProjectItem(String name, String path) { this.name = name; this.path = path; }
    }

    /** Custom renderer for the project list to handle hover effects and styling. */
    private static class ProjectListRenderer extends DefaultListCellRenderer {
        public int hoverIndex = -1;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            ProjectItem item = (ProjectItem) value;
            JPanel panel = new JPanel(new BorderLayout(15, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    if (isSelected || index == hoverIndex) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        if (isSelected) {
                            g2.setColor(UIManager.getColor("List.selectionBackground"));
                        } else {
                            g2.setColor(new Color(255, 255, 255, 15));
                        }
                        g2.fillRoundRect(5, 2, getWidth() - 10, getHeight() - 4, 8, 8);
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            };
            panel.setBorder(new EmptyBorder(10, 15, 10, 15));
            panel.setOpaque(false);
            
            JLabel nameLabel = new JLabel(item.name);
            nameLabel.setFont(UIUtils.getFont(Font.BOLD, 13f));
            nameLabel.setForeground(isSelected ? UIManager.getColor("List.selectionForeground") : UIManager.getColor("Label.foreground"));
            
            JLabel pathLabel = new JLabel(item.path);
            pathLabel.setFont(UIUtils.getFont(Font.PLAIN, 11f));
            pathLabel.setForeground(isSelected ? UIManager.getColor("List.selectionForeground") : UIManager.getColor("Label.disabledForeground"));

            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            textPanel.add(nameLabel);
            textPanel.add(pathLabel);

            panel.add(new JLabel(Icons.FOLDER), BorderLayout.WEST);
            panel.add(textPanel, BorderLayout.CENTER);

            return panel;
        }
    }
}

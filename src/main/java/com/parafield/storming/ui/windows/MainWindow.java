package com.parafield.storming.ui.windows;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.parafield.storming.core.EngineLauncher;
import com.parafield.storming.ui.panels.ConsolePanel;
import com.parafield.storming.ui.panels.HierarchyPanel;
import com.parafield.storming.ui.panels.InspectorPanel;
import com.parafield.storming.ui.panels.NotificationsPanel;
import com.parafield.storming.ui.panels.ProjectBrowserPanel;
import com.parafield.storming.ui.panels.GitPanel;
import com.parafield.storming.ui.panels.PRPanel;
import com.parafield.storming.ui.panels.PRDetailsCenterPanel;
import com.parafield.storming.ui.panels.SceneViewPanel;
import com.parafield.storming.ui.panels.TerminalPanel;
import com.parafield.storming.ui.panels.HierarchyPanel;
import com.parafield.storming.ui.widgets.SideBar;
import com.parafield.storming.ui.widgets.ToolWindow;
import com.parafield.storming.ui.widgets.StormingMenuBar;
import com.parafield.storming.ui.utils.UIAnimator;
import javax.swing.*;
import java.awt.*;

/**
 * The primary window for the Storming Engine Editor.
 * Orchestrates the main layout, including sidebars, split panes, and integrated tool windows.
 * Handles the application's main lifecycle events and UI animations.
 */
public class MainWindow extends JFrame {

    private final ConsolePanel consolePanel;
    private final EngineLauncher engineLauncher;
    private SceneViewPanel sceneViewPanel;
    
    private JSplitPane mainHorizontalSplit; 
    private JSplitPane rightSplit;          
    private JSplitPane centerVerticalSplit; 
    private JSplitPane leftVerticalSplit;

    private StormingMenuBar menuBar;
    private SideBar leftBar;
    private SideBar rightBar;
    private JPanel statusBar;

    private CardLayout rightCardLayout;
    private JPanel rightCardPanel;
    private String currentRightTab = "INSPECTOR";

    private CardLayout leftUpperCardLayout;
    private JPanel leftUpperCardPanel;
    private String currentLeftUpperTab = "HIERARCHY";

    private boolean isLeftOpen = true;
    private boolean isLeftUpperOpen = true;
    private boolean isProjectOpen = true;
    private boolean isRightOpen = true;
    
    private int leftSplitLastLoc = 280;
    private int rightSplitLastLoc = 280; 
    private int leftVerticalSplitLastLoc = 450;

    private JToggleButton hierarchyBtn;
    private JToggleButton commitBtn;
    private JToggleButton prBtn;
    private JToggleButton projectBtn;
    private JToggleButton inspectorBtn;
    private JToggleButton notificationsBtn;
    
    private JTabbedPane editorTabs;
    private static MainWindow instance;

    /**
     * Returns the singleton instance of the MainWindow.
     * @return The current MainWindow instance.
     */
    public static MainWindow getInstance() {
        return instance;
    }

    /**
     * Constructs the MainWindow, initializes UI components, and sets up the engine launcher.
     * Configures window properties and triggers the entrance animation.
     */
    public MainWindow() {
        instance = this;
        setTitle("Storming Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        
        rootPane.putClientProperty("apple.awt.fullWindowContent", true);
        rootPane.putClientProperty("apple.awt.transparentTitleBar", true);
        rootPane.putClientProperty("flatlaf.showWindowIcon", false);

        consolePanel = new ConsolePanel();
        engineLauncher = new EngineLauncher("Engine/2D/build/bin/StormingEngine");
        engineLauncher.addLogListener(consolePanel::log);

        initUI();
        
        // Initial "closed" state for animation
        SwingUtilities.invokeLater(() -> {
            mainHorizontalSplit.setDividerLocation(0);
            // Don't use getWidth() here as it might be 0. Use a large constant.
            rightSplit.setDividerLocation(1400); 
            centerVerticalSplit.setDividerLocation(900);
            
            // Trigger entrance animation after a small delay
            Timer delay = new Timer(300, e -> animateEntrance());
            delay.setRepeats(false);
            delay.start();
        });
    }

    /**
     * Performs the initial unfolding animation for the main editor layout.
     * Animates split panes and fades in sidebars and the menu bar.
     */
    private void animateEntrance() {
        // Unfolding Animation (Drawer style)
        UIAnimator.animateSplit(mainHorizontalSplit, leftSplitLastLoc, 600);
        
        // Animates relative to the RIGHT edge (fixed width)
        UIAnimator.animateSplitTrailing(rightSplit, rightSplitLastLoc, 650);
        
        // Animates relative to the BOTTOM edge (fixed height)
        UIAnimator.animateSplitTrailing(centerVerticalSplit, 250, 700);
        
        // Alpha Fade-in Animation
        UIAnimator.animate(0.0f, 1.0f, 800, alpha -> {
            menuBar.setAlpha(alpha);
            leftBar.setAlpha(alpha);
            rightBar.setAlpha(alpha);
        }, null);
    }

    /**
     * Initializes the complex nested layout of the editor.
     * Sets up split panes, tool windows, sidebars, and the menu bar.
     */
    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // --- 1. Right Side Panels ---
        rightCardLayout = new CardLayout();
        rightCardPanel = new JPanel(rightCardLayout);
        rightCardPanel.add(new ToolWindow("Inspector", new InspectorPanel(), () -> handleRightSidebarClick("INSPECTOR")), "INSPECTOR");
        rightCardPanel.add(new ToolWindow("Notifications", new NotificationsPanel(), () -> handleRightSidebarClick("NOTIFICATIONS")), "NOTIFICATIONS");

        // --- 2. Center & Bottom Panels ---
        editorTabs = new JTabbedPane();
        editorTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        sceneViewPanel = new SceneViewPanel();
        editorTabs.addTab("Scene", sceneViewPanel);
        
        JTabbedPane bottomTabs = new JTabbedPane();
        bottomTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        bottomTabs.addTab("Console", Icons.CONSOLE, consolePanel);
        bottomTabs.addTab("Analyzer", Icons.WARN, new com.parafield.storming.ui.panels.AnalyzerPanel());
        bottomTabs.addTab("Terminal", Icons.TERMINAL, new TerminalPanel());

        // --- 3. Construct Layout Hierarchy ---
        // Top: [ Hierarchy/Commit/PR | [ Scene | Inspector ] ]
        rightSplit = createSplit(JSplitPane.HORIZONTAL_SPLIT, editorTabs, rightCardPanel, 800, 0.0);
        
        leftUpperCardLayout = new CardLayout();
        leftUpperCardPanel = new JPanel(leftUpperCardLayout);
        leftUpperCardPanel.add(new ToolWindow("Hierarchy", new HierarchyPanel(), () -> handleLeftUpperClick("HIERARCHY")), "HIERARCHY");
        leftUpperCardPanel.add(new ToolWindow("Commit", new GitPanel(), () -> handleLeftUpperClick("COMMIT")), "COMMIT");
        leftUpperCardPanel.add(new ToolWindow("Pull Requests", new PRPanel(), () -> handleLeftUpperClick("PR")), "PR");
        
        ProjectBrowserPanel projectPanel = new ProjectBrowserPanel();
        ToolWindow projectTW = new ToolWindow("Project", projectPanel, this::toggleProject);
        
        leftVerticalSplit = createSplit(JSplitPane.VERTICAL_SPLIT, leftUpperCardPanel, projectTW, 450, 0.5);
        mainHorizontalSplit = createSplit(JSplitPane.HORIZONTAL_SPLIT, leftVerticalSplit, rightSplit, 280, 0.0);

        // Final Vertical Split: Top Workspace / Bottom Tabs
        centerVerticalSplit = createSplit(JSplitPane.VERTICAL_SPLIT, mainHorizontalSplit, bottomTabs, 650, 0.0);

        // --- 4. SideBars (JetBrains Style) ---
        leftBar = new SideBar(SwingConstants.VERTICAL, 40);
        leftBar.setAlpha(0.0f);
        hierarchyBtn = (JToggleButton) leftBar.addTab("Hierarchy", Icons.GRID, true, () -> handleLeftUpperClick("HIERARCHY"));
        commitBtn = (JToggleButton) leftBar.addTab("Commit", Icons.COMMIT, true, () -> handleLeftUpperClick("COMMIT"));
        prBtn = (JToggleButton) leftBar.addTab("Pull Requests", Icons.PR, true, () -> handleLeftUpperClick("PR"));
        
        leftBar.addSeparator();
        projectBtn = (JToggleButton) leftBar.addTab("Project", Icons.FOLDER, true, this::toggleProject);
        
        hierarchyBtn.setSelected(true);
        projectBtn.setSelected(true);
        
        rightBar = new SideBar(SwingConstants.VERTICAL, 40);
        rightBar.setAlpha(0.0f);
        inspectorBtn = (JToggleButton) rightBar.addTab("Inspector", Icons.SEARCH, true, () -> handleRightSidebarClick("INSPECTOR"));
        notificationsBtn = (JToggleButton) rightBar.addTab("Notifications", Icons.BELL, true, () -> handleRightSidebarClick("NOTIFICATIONS"));
        inspectorBtn.setSelected(true);

        // --- 5. Assemble ---
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.add(leftBar, BorderLayout.WEST);
        mainContent.add(centerVerticalSplit, BorderLayout.CENTER);
        mainContent.add(rightBar, BorderLayout.EAST);

        menuBar = new StormingMenuBar(this::handlePlay, engineLauncher::stop);
        menuBar.setAlpha(0.0f);
        setJMenuBar(menuBar);
        
        statusBar = createStatusBar();
        
        root.add(mainContent, BorderLayout.CENTER);
        root.add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * Opens a new tab in the editor view displaying details for a specific Pull Request.
     * @param title The title of the Pull Request.
     * @param author The author of the Pull Request.
     */
    public void openPRDetails(String title, String author) {
        PRDetailsCenterPanel centerPanel = new PRDetailsCenterPanel(title, author);
        editorTabs.addTab("PR: " + title.split(":")[0], Icons.PR, centerPanel);
        editorTabs.setSelectedComponent(centerPanel);
    }

    /**
     * Handles clicks on the left sidebar for upper-section tools (Hierarchy, Commit, PR).
     * Manages panel switching and drawer visibility animations.
     * @param tabName The name of the tab to activate.
     */
    private void handleLeftUpperClick(String tabName) {
        if (!isLeftUpperOpen) {
            leftUpperCardLayout.show(leftUpperCardPanel, tabName);
            currentLeftUpperTab = tabName;
            UIAnimator.animateSplit(leftVerticalSplit, leftVerticalSplitLastLoc, 250);
            isLeftUpperOpen = true;
            if (!isLeftOpen) toggleLeftDrawer();
        } else {
            if (currentLeftUpperTab.equals(tabName)) {
                if (isProjectOpen) {
                    leftVerticalSplitLastLoc = leftVerticalSplit.getDividerLocation();
                    UIAnimator.animateSplit(leftVerticalSplit, 0, 250);
                } else {
                    toggleLeftDrawer();
                }
                isLeftUpperOpen = false;
            } else {
                leftUpperCardLayout.show(leftUpperCardPanel, tabName);
                currentLeftUpperTab = tabName;
            }
        }
        updateLeftBar();
    }

    /**
     * Toggles the visibility of the Project Browser panel with animation.
     */
    private void toggleProject() {
        if (isProjectOpen) {
            if (isLeftUpperOpen) {
                leftVerticalSplitLastLoc = leftVerticalSplit.getDividerLocation();
                UIAnimator.animateSplit(leftVerticalSplit, leftVerticalSplit.getHeight(), 250);
            } else {
                toggleLeftDrawer();
            }
        } else {
            if (isLeftUpperOpen) {
                UIAnimator.animateSplit(leftVerticalSplit, leftVerticalSplitLastLoc, 250);
            } else {
                toggleLeftDrawer();
            }
        }
        isProjectOpen = !isProjectOpen;
        updateLeftBar();
    }

    /**
     * Toggles the entire left side drawer (containing Hierarchy and Project Browser).
     */
    private void toggleLeftDrawer() {
        if (isLeftOpen) {
            leftSplitLastLoc = mainHorizontalSplit.getDividerLocation();
            UIAnimator.animateSplit(mainHorizontalSplit, 0, 250);
        } else {
            UIAnimator.animateSplit(mainHorizontalSplit, leftSplitLastLoc, 250);
        }
        isLeftOpen = !isLeftOpen;
    }

    /**
     * Syncs the selection state of the left sidebar buttons with the current drawer state.
     */
    private void updateLeftBar() {
        hierarchyBtn.setSelected(isLeftUpperOpen && isLeftOpen && currentLeftUpperTab.equals("HIERARCHY"));
        commitBtn.setSelected(isLeftUpperOpen && isLeftOpen && currentLeftUpperTab.equals("COMMIT"));
        prBtn.setSelected(isLeftUpperOpen && isLeftOpen && currentLeftUpperTab.equals("PR"));
        projectBtn.setSelected(isProjectOpen && isLeftOpen);
        
        // If both are closed, close the whole drawer
        if (!isLeftUpperOpen && !isProjectOpen && isLeftOpen) {
            toggleLeftDrawer();
        } 
        // If opening one when drawer is closed, open the drawer
        else if ((isLeftUpperOpen || isProjectOpen) && !isLeftOpen) {
            toggleLeftDrawer();
        }
    }

    /**
     * Handles clicks on the right sidebar for tools (Inspector, Notifications).
     * @param tabName The name of the tab to activate.
     */
    private void handleRightSidebarClick(String tabName) {
        if (!isRightOpen) {
            rightCardLayout.show(rightCardPanel, tabName);
            currentRightTab = tabName;
            UIAnimator.animateSplit(rightSplit, rightSplit.getWidth() - rightSplitLastLoc, 250);
            isRightOpen = true;
        } else {
            if (currentRightTab.equals(tabName)) {
                rightSplitLastLoc = rightSplit.getWidth() - rightSplit.getDividerLocation();
                UIAnimator.animateSplit(rightSplit, rightSplit.getWidth(), 250);
                isRightOpen = false;
            } else {
                rightCardLayout.show(rightCardPanel, tabName);
                currentRightTab = tabName;
            }
        }
        inspectorBtn.setSelected(isRightOpen && currentRightTab.equals("INSPECTOR"));
        notificationsBtn.setSelected(isRightOpen && currentRightTab.equals("NOTIFICATIONS"));
    }

    /**
     * Utility method to create a stylized JSplitPane.
     * @param orient Orientation (HORIZONTAL_SPLIT or VERTICAL_SPLIT).
     * @param left The left/top component.
     * @param right The right/bottom component.
     * @param loc Initial divider location.
     * @param weight Resize weight.
     * @return A configured JSplitPane.
     */
    private JSplitPane createSplit(int orient, JComponent left, JComponent right, int loc, double weight) {
        JSplitPane split = new JSplitPane(orient, left, right);
        split.setDividerLocation(loc);
        split.setDividerSize(3);
        split.setBorder(null);
        split.setResizeWeight(weight);
        return split;
    }

    /**
     * Launches the engine simulation. Prompts for restart if already running.
     */
    private void handlePlay() {
        if (engineLauncher.isRunning()) {
            int result = JOptionPane.showConfirmDialog(this, "Restart simulation?", "Running", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                engineLauncher.stop();
                Timer timer = new Timer(500, e -> new SimulationWindow(engineLauncher).startSimulation());
                timer.setRepeats(false); timer.start();
            }
        } else {
            new SimulationWindow(engineLauncher).startSimulation();
        }
    }

    /**
     * Creates the editor's status bar, displaying engine stats, Git branch, and system resources.
     * @return A JPanel configured as a status bar.
     */
    private JPanel createStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(0, 26));
        p.setBackground(new Color(25, 25, 30));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));
        
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        left.setOpaque(false);
        JLabel apiLabel = new JLabel("  ● OpenGL 4.5 Core");
        apiLabel.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 11f));
        apiLabel.setForeground(new Color(46, 204, 113));
        left.add(apiLabel);
        
        JLabel branchLabel = new JLabel("master", Icons.GIT, SwingConstants.LEFT);
        branchLabel.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 11f));
        branchLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        left.add(branchLabel);
        
        JLabel statsLabel = new JLabel("Draw Calls: 0 | Sprites: 0");
        statsLabel.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 11f));
        statsLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        left.add(statsLabel);
        
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 2));
        right.setOpaque(false);
        
        JProgressBar memBar = new JProgressBar(0, 100);
        memBar.setPreferredSize(new Dimension(100, 8));
        memBar.putClientProperty("JProgressBar.largeHeight", false);
        memBar.setForeground(new Color(52, 152, 219));
        memBar.setBackground(new Color(40, 40, 45));
        memBar.setBorder(null);
        
        JLabel memText = new JLabel("-- / --");
        memText.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 11f));
        memText.setForeground(UIManager.getColor("Label.disabledForeground"));
        
        Timer t = new Timer(2000, e -> {
            Runtime r = Runtime.getRuntime();
            long total = r.totalMemory() / 1024 / 1024;
            long used = (r.totalMemory() - r.freeMemory()) / 1024 / 1024;
            memText.setText(String.format("%dMB / %dMB", used, total));
            memBar.setValue((int)((double)used / total * 100));
        });
        t.start();
        
        right.add(memText);
        right.add(memBar);
        right.add(new JLabel("UTF-8  "));
        
        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }
}

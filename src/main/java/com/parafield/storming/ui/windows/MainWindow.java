package com.parafield.storming.ui.windows;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.parafield.storming.core.EngineLauncher;
import com.parafield.storming.ui.panels.ConsolePanel;
import com.parafield.storming.ui.panels.SceneViewPanel;
import com.parafield.storming.ui.widgets.MainToolbar;
import com.parafield.storming.ui.widgets.SideBar;
import com.parafield.storming.ui.widgets.ToolWindow;
import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private final ConsolePanel consolePanel;
    private final EngineLauncher engineLauncher;
    private SceneViewPanel sceneViewPanel;
    
    private JSplitPane mainSplit;
    private JSplitPane rightSplit;
    private JSplitPane bottomSplit;

    public MainWindow() {
        setIconImage(Icons.FRAME_ICON);
        setTitle("Storming Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        
        // Use modern title bar if supported
        rootPane.putClientProperty("apple.awt.fullWindowContent", true);
        rootPane.putClientProperty("apple.awt.transparentTitleBar", true);

        // Initialize Core Logic
        consolePanel = new ConsolePanel();
        engineLauncher = new EngineLauncher("Engine/2D/build/bin/StormingEngine", consolePanel::log);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // 1. TOP TOOLBAR
        root.add(new MainToolbar(this::handlePlay, engineLauncher::stop), BorderLayout.NORTH);

        // 2. CENTER CONTENT AREA
        JPanel centerArea = new JPanel(new BorderLayout());
        
        // SideBars
        SideBar leftBar = new SideBar(SwingConstants.VERTICAL);
        leftBar.addTab("Project", Icons.FOLDER, true, () -> togglePanel(bottomSplit, true, 280));
        
        SideBar rightBar = new SideBar(SwingConstants.VERTICAL);
        rightBar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Component.borderColor")));
        rightBar.addTab("Inspector", Icons.SEARCH, true, () -> togglePanel(rightSplit, false, 300));
        rightBar.addTab("Notifications", Icons.BELL, true, () -> togglePanel(rightSplit, false, 300));

        // Center Panel (The Editor/Scene View)
        JTabbedPane editorTabs = new JTabbedPane();
        editorTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        editorTabs.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        
        sceneViewPanel = new SceneViewPanel();
        editorTabs.addTab("Scene", sceneViewPanel);
        editorTabs.addTab("Game", new JPanel());

        // Construct the splits
        ToolWindow hierarchyTW = new ToolWindow("Hierarchy", new JTree());
        ToolWindow inspectorTW = new ToolWindow("Inspector", new JTextArea("Select an object..."));
        
        // Bottom Tools (Console, Analyzer)
        JTabbedPane bottomTabs = new JTabbedPane();
        bottomTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        bottomTabs.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        bottomTabs.addTab("Console", Icons.CONSOLE, consolePanel);
        bottomTabs.addTab("Analyzer", Icons.WARN, new com.parafield.storming.ui.panels.AnalyzerPanel());

        rightSplit = createSplit(JSplitPane.HORIZONTAL_SPLIT, editorTabs, inspectorTW, 1050);
        mainSplit = createSplit(JSplitPane.VERTICAL_SPLIT, rightSplit, bottomTabs, 650);
        bottomSplit = createSplit(JSplitPane.HORIZONTAL_SPLIT, hierarchyTW, mainSplit, 280);

        centerArea.add(leftBar, BorderLayout.WEST);
        centerArea.add(bottomSplit, BorderLayout.CENTER);
        centerArea.add(rightBar, BorderLayout.EAST);

        root.add(centerArea, BorderLayout.CENTER);

        // 3. STATUS BAR
        root.add(createStatusBar(), BorderLayout.SOUTH);
    }

    private void handlePlay() {
        if (engineLauncher.isRunning()) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "A simulation is already running. Do you want to restart it?",
                "Simulation Running",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                engineLauncher.stop();
                Timer timer = new Timer(500, e -> {
                    SimulationWindow sim = new SimulationWindow(engineLauncher);
                    sim.startSimulation();
                });
                timer.setRepeats(false);
                timer.start();
            }
        } else {
            SimulationWindow sim = new SimulationWindow(engineLauncher);
            sim.startSimulation();
        }
    }

    private int hierarchyLastLoc = 280;
    private int inspectorLastLoc = 1050;
    private int consoleLastLoc = 650;

    private void togglePanel(JSplitPane splitPane, boolean isLeftPanel, int defaultOpenLocation) {
        int currentDividerLocation = splitPane.getDividerLocation();
        int splitPaneWidthOrHeight = isLeftPanel ? splitPane.getWidth() : splitPane.getHeight();

        // Determine if the panel is currently open or collapsed
        boolean isOpen;
        if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
            isOpen = isLeftPanel ? (currentDividerLocation > 5) : (currentDividerLocation < splitPaneWidthOrHeight - 5);
        } else { // VERTICAL_SPLIT
            isOpen = isLeftPanel ? (currentDividerLocation > 5) : (currentDividerLocation < splitPaneWidthOrHeight - 5);
        }

        if (isOpen) {
            // Panel is open, collapse it
            if (isLeftPanel) {
                if (splitPane == bottomSplit) hierarchyLastLoc = currentDividerLocation;
                else if (splitPane == rightSplit) inspectorLastLoc = currentDividerLocation;
                else if (splitPane == mainSplit) consoleLastLoc = currentDividerLocation;

                splitPane.setDividerLocation(0); // Collapse left
            } else {
                if (splitPane == bottomSplit) hierarchyLastLoc = currentDividerLocation;
                else if (splitPane == rightSplit) inspectorLastLoc = currentDividerLocation;
                else if (splitPane == mainSplit) consoleLastLoc = currentDividerLocation;
                
                splitPane.setDividerLocation(splitPaneWidthOrHeight); // Collapse right
            }
        } else {
            // Panel is collapsed, expand it to its last known location or default
            int targetLocation;
            if (isLeftPanel) {
                targetLocation = (splitPane == bottomSplit) ? hierarchyLastLoc : (splitPane == rightSplit ? inspectorLastLoc : consoleLastLoc);
            } else {
                targetLocation = (splitPane == bottomSplit) ? hierarchyLastLoc : (splitPane == rightSplit ? inspectorLastLoc : consoleLastLoc);
            }
            splitPane.setDividerLocation(targetLocation);
        }
    }

    private JSplitPane createSplit(int orient, JComponent left, JComponent right, int loc) {
        JSplitPane split = new JSplitPane(orient, left, right);
        split.setDividerLocation(loc);
        split.setDividerSize(3);
        split.setBorder(null);
        split.putClientProperty("JSplitPane.style", "thin");
        return split;
    }

    private JPanel createStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(0, 25));
        p.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 5%)");
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));
        
        JLabel status = new JLabel("  ● Running: OpenGL 4.5 | Storming Core v1.0");
        status.setFont(new Font("Inter", Font.PLAIN, 11));
        status.setForeground(UIManager.getColor("Label.disabledForeground"));
        
        p.add(status, BorderLayout.WEST);
        
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightInfo.setOpaque(false);
        JLabel branch = new JLabel("main*");
        branch.setFont(new Font("Inter", Font.PLAIN, 11));
        rightInfo.add(branch);
        rightInfo.add(new JLabel("UTF-8 "));
        
        p.add(rightInfo, BorderLayout.EAST);
        return p;
    }
}

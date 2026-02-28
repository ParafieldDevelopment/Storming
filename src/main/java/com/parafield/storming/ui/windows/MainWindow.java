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
    
    private JSplitPane mainHorizontalSplit; 
    private JSplitPane rightSplit;          
    private JSplitPane centerVerticalSplit; 

    private CardLayout rightCardLayout;
    private JPanel rightCardPanel;
    private String currentRightTab = "INSPECTOR";

    private boolean isLeftOpen = true;
    private boolean isRightOpen = true;
    private int leftSplitLastLoc = 280;
    private int rightSplitLastLoc = 300; 

    private JToggleButton projectBtn;
    private JToggleButton inspectorBtn;
    private JToggleButton notificationsBtn;

    public MainWindow() {
        setIconImage(Icons.FRAME_ICON);
        setTitle("Storming Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        
        rootPane.putClientProperty("apple.awt.fullWindowContent", true);
        rootPane.putClientProperty("apple.awt.transparentTitleBar", true);

        consolePanel = new ConsolePanel();
        engineLauncher = new EngineLauncher("Engine/2D/build/bin/StormingEngine", consolePanel::log);

        initUI();
        
        // Ensure dividers are set after layout is ready
        SwingUtilities.invokeLater(() -> {
            mainHorizontalSplit.setDividerLocation(leftSplitLastLoc);
            rightSplit.setDividerLocation(rightSplit.getWidth() - rightSplitLastLoc);
        });
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // --- 1. Right Side Panels ---
        rightCardLayout = new CardLayout();
        rightCardPanel = new JPanel(rightCardLayout);
        rightCardPanel.add(new ToolWindow("Inspector", new JTextArea("Select an object...")), "INSPECTOR");
        rightCardPanel.add(new ToolWindow("Notifications", new JTextArea("No new notifications.")), "NOTIFICATIONS");

        // --- 2. Center Panels ---
        JTabbedPane editorTabs = new JTabbedPane();
        editorTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        sceneViewPanel = new SceneViewPanel();
        editorTabs.addTab("Scene", sceneViewPanel);
        
        JTabbedPane bottomTabs = new JTabbedPane();
        bottomTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        bottomTabs.addTab("Console", Icons.CONSOLE, consolePanel);
        bottomTabs.addTab("Analyzer", Icons.WARN, new com.parafield.storming.ui.panels.AnalyzerPanel());

        centerVerticalSplit = createSplit(JSplitPane.VERTICAL_SPLIT, editorTabs, bottomTabs, 600, 0.7);

        // --- 3. Independent Splits ---
        rightSplit = createSplit(JSplitPane.HORIZONTAL_SPLIT, centerVerticalSplit, rightCardPanel, 1100, 1.0);
        
        ToolWindow hierarchyTW = new ToolWindow("Hierarchy", new JTree());
        mainHorizontalSplit = createSplit(JSplitPane.HORIZONTAL_SPLIT, hierarchyTW, rightSplit, 280, 0.0);

        // --- 4. SideBars (JetBrains Style) ---
        SideBar leftBar = new SideBar(SwingConstants.VERTICAL, 40);
        projectBtn = (JToggleButton) leftBar.addTab("Project", Icons.FOLDER, true, this::toggleLeftPanel);
        projectBtn.setSelected(true);
        
        leftBar.add(Box.createVerticalGlue());
        
        SideBar rightBar = new SideBar(SwingConstants.VERTICAL, 40);
        inspectorBtn = (JToggleButton) rightBar.addTab("Inspector", Icons.SEARCH, true, () -> handleRightSidebarClick("INSPECTOR"));
        notificationsBtn = (JToggleButton) rightBar.addTab("Notifications", Icons.BELL, true, () -> handleRightSidebarClick("NOTIFICATIONS"));
        inspectorBtn.setSelected(true);

        // --- 5. Assemble ---
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.add(leftBar, BorderLayout.WEST);
        mainContent.add(mainHorizontalSplit, BorderLayout.CENTER);
        mainContent.add(rightBar, BorderLayout.EAST);

        root.add(new MainToolbar(this::handlePlay, engineLauncher::stop), BorderLayout.NORTH);
        root.add(mainContent, BorderLayout.CENTER);
        root.add(createStatusBar(), BorderLayout.SOUTH);
    }

    private void toggleLeftPanel() {
        if (isLeftOpen) {
            leftSplitLastLoc = mainHorizontalSplit.getDividerLocation();
            mainHorizontalSplit.setDividerLocation(0);
        } else {
            mainHorizontalSplit.setDividerLocation(leftSplitLastLoc);
        }
        isLeftOpen = !isLeftOpen;
        projectBtn.setSelected(isLeftOpen);
    }

    private void handleRightSidebarClick(String tabName) {
        if (!isRightOpen) {
            rightCardLayout.show(rightCardPanel, tabName);
            currentRightTab = tabName;
            rightSplit.setDividerLocation(rightSplit.getWidth() - rightSplitLastLoc);
            isRightOpen = true;
        } else {
            if (currentRightTab.equals(tabName)) {
                rightSplitLastLoc = rightSplit.getWidth() - rightSplit.getDividerLocation();
                rightSplit.setDividerLocation(rightSplit.getWidth());
                isRightOpen = false;
            } else {
                rightCardLayout.show(rightCardPanel, tabName);
                currentRightTab = tabName;
            }
        }
        inspectorBtn.setSelected(isRightOpen && currentRightTab.equals("INSPECTOR"));
        notificationsBtn.setSelected(isRightOpen && currentRightTab.equals("NOTIFICATIONS"));
    }

    private JSplitPane createSplit(int orient, JComponent left, JComponent right, int loc, double weight) {
        JSplitPane split = new JSplitPane(orient, left, right);
        split.setDividerLocation(loc);
        split.setDividerSize(3);
        split.setBorder(null);
        split.setResizeWeight(weight);
        return split;
    }

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

    private JPanel createStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(0, 26));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));
        
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        left.setOpaque(false);
        left.add(new JLabel("  ● OpenGL 4.5 Core"));
        left.add(new JLabel(" |  Branch: master"));
        
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 2));
        right.setOpaque(false);
        JLabel mem = new JLabel("Memory: -- / --");
        mem.setForeground(UIManager.getColor("Label.disabledForeground"));
        
        Timer t = new Timer(2000, e -> {
            Runtime r = Runtime.getRuntime();
            long total = r.totalMemory() / 1024 / 1024;
            long used = (r.totalMemory() - r.freeMemory()) / 1024 / 1024;
            mem.setText(String.format("Memory: %dMB / %dMB", used, total));
        });
        t.start();
        
        right.add(mem);
        right.add(new JLabel("UTF-8  "));
        
        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }
}

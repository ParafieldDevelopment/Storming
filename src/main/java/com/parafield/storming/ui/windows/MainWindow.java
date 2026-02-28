package com.parafield.storming.ui.windows;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.parafield.storming.core.EngineLauncher;
import com.parafield.storming.ui.panels.ConsolePanel;
import com.parafield.storming.ui.panels.HierarchyPanel;
import com.parafield.storming.ui.panels.InspectorPanel;
import com.parafield.storming.ui.panels.NotificationsPanel;
import com.parafield.storming.ui.panels.SceneViewPanel;
import com.parafield.storming.ui.panels.TerminalPanel;
import com.parafield.storming.ui.widgets.MainToolbar;
import com.parafield.storming.ui.widgets.SideBar;
import com.parafield.storming.ui.widgets.ToolWindow;
import com.parafield.storming.ui.widgets.StormingMenuBar;
import com.parafield.storming.ui.utils.UIAnimator;
import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private final ConsolePanel consolePanel;
    private final EngineLauncher engineLauncher;
    private SceneViewPanel sceneViewPanel;
    
    private JSplitPane mainHorizontalSplit; 
    private JSplitPane rightSplit;          
    private JSplitPane centerVerticalSplit; 

    private StormingMenuBar menuBar;
    private SideBar leftBar;
    private SideBar rightBar;
    private JPanel statusBar;

    private CardLayout rightCardLayout;
    private JPanel rightCardPanel;
    private String currentRightTab = "INSPECTOR";

    private boolean isLeftOpen = true;
    private boolean isRightOpen = true;
    private int leftSplitLastLoc = 280;
    private int rightSplitLastLoc = 280; 

    private JToggleButton projectBtn;
    private JToggleButton inspectorBtn;
    private JToggleButton notificationsBtn;

    public MainWindow() {
        setTitle("Storming Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        
        rootPane.putClientProperty("apple.awt.fullWindowContent", true);
        rootPane.putClientProperty("apple.awt.transparentTitleBar", true);
        rootPane.putClientProperty("flatlaf.showWindowIcon", false);

        consolePanel = new ConsolePanel();
        engineLauncher = new EngineLauncher("Engine/2D/build/bin/StormingEngine", consolePanel::log);

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

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // --- 1. Right Side Panels ---
        rightCardLayout = new CardLayout();
        rightCardPanel = new JPanel(rightCardLayout);
        rightCardPanel.add(new ToolWindow("Inspector", new InspectorPanel()), "INSPECTOR");
        rightCardPanel.add(new ToolWindow("Notifications", new NotificationsPanel()), "NOTIFICATIONS");

        // --- 2. Center & Bottom Panels ---
        JTabbedPane editorTabs = new JTabbedPane();
        editorTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        sceneViewPanel = new SceneViewPanel();
        editorTabs.addTab("Scene", sceneViewPanel);
        
        JTabbedPane bottomTabs = new JTabbedPane();
        bottomTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        bottomTabs.addTab("Console", Icons.CONSOLE, consolePanel);
        bottomTabs.addTab("Analyzer", Icons.WARN, new com.parafield.storming.ui.panels.AnalyzerPanel());
        bottomTabs.addTab("Terminal", Icons.TERMINAL, new TerminalPanel());

        // --- 3. Construct Layout Hierarchy ---
        // Top: [ Hierarchy | [ Scene | Inspector ] ]
        rightSplit = createSplit(JSplitPane.HORIZONTAL_SPLIT, editorTabs, rightCardPanel, 800, 0.0);
        
        HierarchyPanel hierarchyPanel = new HierarchyPanel();
        ToolWindow hierarchyTW = new ToolWindow("Hierarchy", hierarchyPanel);
        mainHorizontalSplit = createSplit(JSplitPane.HORIZONTAL_SPLIT, hierarchyTW, rightSplit, 280, 0.0);

        // Final Vertical Split: Top Workspace / Bottom Tabs
        centerVerticalSplit = createSplit(JSplitPane.VERTICAL_SPLIT, mainHorizontalSplit, bottomTabs, 650, 0.0);

        // --- 4. SideBars (JetBrains Style) ---
        leftBar = new SideBar(SwingConstants.VERTICAL, 40);
        leftBar.setAlpha(0.0f);
        projectBtn = (JToggleButton) leftBar.addTab("Project", Icons.FOLDER, true, this::toggleLeftPanel);
        projectBtn.setSelected(true);
        
        leftBar.add(Box.createVerticalGlue());
        
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

    private void toggleLeftPanel() {
        if (isLeftOpen) {
            leftSplitLastLoc = mainHorizontalSplit.getDividerLocation();
            UIAnimator.animateSplit(mainHorizontalSplit, 0, 250);
        } else {
            UIAnimator.animateSplit(mainHorizontalSplit, leftSplitLastLoc, 250);
        }
        isLeftOpen = !isLeftOpen;
        projectBtn.setSelected(isLeftOpen);
    }

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
        p.setBackground(new Color(25, 25, 30));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));
        
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        left.setOpaque(false);
        JLabel apiLabel = new JLabel("  ● OpenGL 4.5 Core");
        apiLabel.setFont(new Font("Inter", Font.BOLD, 11));
        apiLabel.setForeground(new Color(46, 204, 113));
        left.add(apiLabel);
        
        JLabel branchLabel = new JLabel("master", Icons.GIT, SwingConstants.LEFT);
        branchLabel.setFont(new Font("Inter", Font.PLAIN, 11));
        branchLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        left.add(branchLabel);
        
        JLabel statsLabel = new JLabel("Draw Calls: 0 | Sprites: 0");
        statsLabel.setFont(new Font("Inter", Font.PLAIN, 11));
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
        memText.setFont(new Font("Inter", Font.PLAIN, 11));
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

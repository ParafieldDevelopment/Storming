package com.parafield.storming.ui.windows;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.parafield.storming.Icons;
import com.parafield.storming.core.DiscordRPCManager;
import com.parafield.storming.core.EngineLauncher;
import com.parafield.storming.core.ProjectManager;
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
import com.parafield.storming.ui.widgets.SideBar;
import com.parafield.storming.ui.widgets.ToolWindow;
import com.parafield.storming.ui.widgets.StormingMenuBar;
import com.parafield.storming.ui.utils.UIAnimator;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * The primary window for the Storming Engine Editor.
 * Orchestrates the main layout and handles engine communication.
 */
public class MainWindow extends JFrame {

    private final ConsolePanel consolePanel;
    private final EngineLauncher editorLauncher;
    private final EngineLauncher simulationLauncher;
    private SceneViewPanel sceneViewPanel;
    private HierarchyPanel hierarchyPanel;
    private InspectorPanel inspectorPanel;
    
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
    private final File projectRoot;

    public static MainWindow getInstance() {
        return instance;
    }

    public EngineLauncher getEngineLauncher() {
        return editorLauncher;
    }

    /**
     * Constructs the MainWindow and loads the specified project.
     * @param projectPath The path to the project to open.
     */
    public MainWindow(String projectPath) {
        instance = this;
        this.projectRoot = new File(projectPath);
        ProjectManager.addRecentProject(projectPath);
        
        setTitle("Storming Engine - " + projectRoot.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        
        DiscordRPCManager.updateActivity("Editing Project", projectRoot.getName());
        
        rootPane.putClientProperty("apple.awt.fullWindowContent", true);
        rootPane.putClientProperty("apple.awt.transparentTitleBar", true);
        rootPane.putClientProperty("flatlaf.showWindowIcon", false);

        String enginePath = "Engine/2D/build/bin/StormingEngine";
        consolePanel = new ConsolePanel();
        
        // Link Discord RPC to internal console
        DiscordRPCManager.addLogListener(consolePanel::log);
        
        editorLauncher = new EngineLauncher(enginePath);
        editorLauncher.addLogListener(consolePanel::log);
        editorLauncher.addTelemetryListener(this::handleGlobalTelemetry);

        simulationLauncher = new EngineLauncher(enginePath);
        simulationLauncher.addLogListener(msg -> consolePanel.log("[Sim] " + msg));

        initUI();
        loadProject();
        startBackgroundEngine();
        
        SwingUtilities.invokeLater(() -> {
            mainHorizontalSplit.setDividerLocation(0);
            rightSplit.setDividerLocation(1400); 
            centerVerticalSplit.setDividerLocation(900);
            Timer delay = new Timer(300, e -> animateEntrance());
            delay.setRepeats(false);
            delay.start();
        });
    }

    private void handleGlobalTelemetry(String jsonStr) {
        try {
            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
            if (!json.has("type")) return;
            
            String type = json.get("type").getAsString();
            
            if ("scene_tree".equals(type)) {
                List<HierarchyPanel.EntityItem> entities = new ArrayList<>();
                json.get("entities").getAsJsonArray().forEach(e -> {
                    JsonObject obj = e.getAsJsonObject();
                    entities.add(new HierarchyPanel.EntityItem(obj.get("id").getAsInt(), obj.get("name").getAsString()));
                });
                updateHierarchy(entities);
            } else if ("entity_details".equals(type)) {
                if (inspectorPanel != null) {
                    inspectorPanel.updateDetails(json);
                }
            } else if ("scene_data_dump".equals(type)) {
                performDiskSave(json.get("data").getAsString());
            }
        } catch (Exception ignored) {}
    }

    public void updateHierarchy(List<HierarchyPanel.EntityItem> entities) {
        if (hierarchyPanel != null) {
            hierarchyPanel.updateHierarchyFromItems(entities);
        }
    }

    /**
     * Handles the selection of an entity from the hierarchy.
     * Requests component data from the engine for the selected entity.
     * @param entity The selected entity item.
     */
    public void onEntitySelected(HierarchyPanel.EntityItem entity) {
        editorLauncher.sendCommand("{\"type\":\"command\",\"action\":\"select_entity\",\"id\":" + entity.id() + "}");
    }

    private void startBackgroundEngine() {
        String shm = "/storming_editor_" + System.currentTimeMillis();
        editorLauncher.launch(shm, true);
        sceneViewPanel.startStreaming(shm);
        
        Timer t = new Timer(1500, e -> {
            File[] files = projectRoot.listFiles((dir, name) -> name.endsWith(".storm"));
            if (files != null && files.length > 0) {
                try {
                    String content = Files.readString(files[0].toPath());
                    JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                    String mainScenePath = json.get("main_scene").getAsString();
                    File sceneFile = new File(projectRoot, mainScenePath);
                    
                    editorLauncher.sendCommand(String.format(
                        "{\"type\":\"command\",\"action\":\"load_scene\",\"path\":\"%s\"}", 
                        sceneFile.getAbsolutePath().replace("\\", "/")
                    ));
                } catch (Exception ignored) {}
            }
        });
        t.setRepeats(false);
        t.start();
    }

    /**
     * Loads project metadata from the .storm file and populates the editor state.
     */
    private void loadProject() {
        try {
            File[] files = projectRoot.listFiles((dir, name) -> name.endsWith(".storm"));
            if (files != null && files.length > 0) {
                String content = Files.readString(files[0].toPath());
                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                
                String mainScenePath = json.get("main_scene").getAsString();
                File sceneFile = new File(projectRoot, mainScenePath);
                
                if (sceneFile.exists()) {
                    loadScene(sceneFile);
                }
            }
        } catch (Exception e) {
            consolePanel.log("[Error] Failed to load project: " + e.getMessage());
        }
    }

    /**
     * Loads a scene file and populates the hierarchy panel.
     * @param sceneFile The .storm_scene file to load.
     */
    private void loadScene(File sceneFile) {
        try {
            String content = Files.readString(sceneFile.toPath());
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            
            List<HierarchyPanel.EntityItem> entities = new ArrayList<>();
            json.get("entities").getAsJsonArray().forEach(e -> {
                entities.add(new HierarchyPanel.EntityItem(-1, e.getAsJsonObject().get("name").getAsString()));
            });
            
            updateHierarchy(entities);
            consolePanel.log("[System] Loaded scene: " + sceneFile.getName());
            DiscordRPCManager.updateActivity("Editing Scene: " + sceneFile.getName(), projectRoot.getName());
        } catch (Exception e) {
            consolePanel.log("[Error] Failed to load scene: " + e.getMessage());
        }
    }

    /**
     * Triggers a save request to the background engine.
     */
    public void saveScene() {
        consolePanel.log("[System] Requesting scene save from engine...");
        editorLauncher.sendCommand("{\"type\":\"command\",\"action\":\"request_save\"}");
    }

    private void performDiskSave(String jsonData) {
        try {
            File[] files = projectRoot.listFiles((dir, name) -> name.endsWith(".storm"));
            if (files == null || files.length == 0) return;
            String projectContent = Files.readString(files[0].toPath());
            String mainScenePath = JsonParser.parseString(projectContent).getAsJsonObject().get("main_scene").getAsString();
            File sceneFile = new File(projectRoot, mainScenePath);

            Files.writeString(sceneFile.toPath(), jsonData);
            consolePanel.log("[System] Scene saved successfully: " + sceneFile.getName());
        } catch (Exception e) {
            consolePanel.log("[Error] Disk Save failed: " + e.getMessage());
        }
    }

    private void animateEntrance() {
        UIAnimator.animateSplit(mainHorizontalSplit, leftSplitLastLoc, 600);
        UIAnimator.animateSplitTrailing(rightSplit, rightSplitLastLoc, 650);
        UIAnimator.animateSplitTrailing(centerVerticalSplit, 250, 700);
        UIAnimator.animate(0.0f, 1.0f, 800, alpha -> {
            menuBar.setAlpha(alpha);
            leftBar.setAlpha(alpha);
            rightBar.setAlpha(alpha);
        }, null);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        rightCardLayout = new CardLayout();
        rightCardPanel = new JPanel(rightCardLayout);
        
        inspectorPanel = new InspectorPanel();
        rightCardPanel.add(new ToolWindow("Inspector", inspectorPanel, () -> handleRightSidebarClick("INSPECTOR")), "INSPECTOR");
        rightCardPanel.add(new ToolWindow("Notifications", new NotificationsPanel(), () -> handleRightSidebarClick("NOTIFICATIONS")), "NOTIFICATIONS");

        editorTabs = new JTabbedPane();
        editorTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        sceneViewPanel = new SceneViewPanel();
        editorTabs.addTab("Scene", sceneViewPanel);
        
        JTabbedPane bottomTabs = new JTabbedPane();
        bottomTabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        bottomTabs.addTab("Console", Icons.CONSOLE, consolePanel);
        bottomTabs.addTab("Analyzer", Icons.WARN, new com.parafield.storming.ui.panels.AnalyzerPanel());
        bottomTabs.addTab("Terminal", Icons.TERMINAL, new TerminalPanel());

        rightSplit = createSplit(JSplitPane.HORIZONTAL_SPLIT, editorTabs, rightCardPanel, 800, 0.0);
        
        leftUpperCardLayout = new CardLayout();
        leftUpperCardPanel = new JPanel(leftUpperCardLayout);
        
        hierarchyPanel = new HierarchyPanel();
        leftUpperCardPanel.add(new ToolWindow("Hierarchy", hierarchyPanel, () -> handleLeftUpperClick("HIERARCHY")), "HIERARCHY");
        leftUpperCardPanel.add(new ToolWindow("Commit", new GitPanel(), () -> handleLeftUpperClick("COMMIT")), "COMMIT");
        leftUpperCardPanel.add(new ToolWindow("Pull Requests", new PRPanel(), () -> handleLeftUpperClick("PR")), "PR");
        
        ProjectBrowserPanel projectPanel = new ProjectBrowserPanel(projectRoot);
        ToolWindow projectTW = new ToolWindow("Project", projectPanel, this::toggleProject);
        
        leftVerticalSplit = createSplit(JSplitPane.VERTICAL_SPLIT, leftUpperCardPanel, projectTW, 450, 0.5);
        mainHorizontalSplit = createSplit(JSplitPane.HORIZONTAL_SPLIT, leftVerticalSplit, rightSplit, 280, 0.0);

        centerVerticalSplit = createSplit(JSplitPane.VERTICAL_SPLIT, mainHorizontalSplit, bottomTabs, 650, 0.0);

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

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.add(leftBar, BorderLayout.WEST);
        mainContent.add(centerVerticalSplit, BorderLayout.CENTER);
        mainContent.add(rightBar, BorderLayout.EAST);

        menuBar = new StormingMenuBar(this::handlePlay, simulationLauncher::stop);
        menuBar.setAlpha(0.0f);
        menuBar.setProjectName(projectRoot.getName());
        setJMenuBar(menuBar);
        
        statusBar = createStatusBar();
        
        root.add(mainContent, BorderLayout.CENTER);
        root.add(statusBar, BorderLayout.SOUTH);
    }

    public void openPRDetails(String title, String author) {
        PRDetailsCenterPanel centerPanel = new PRDetailsCenterPanel(title, author);
        editorTabs.addTab("PR: " + title.split(":")[0], Icons.PR, centerPanel);
        editorTabs.setSelectedComponent(centerPanel);
    }

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

    private void toggleLeftDrawer() {
        if (isLeftOpen) {
            leftSplitLastLoc = mainHorizontalSplit.getDividerLocation();
            UIAnimator.animateSplit(mainHorizontalSplit, 0, 250);
        } else {
            UIAnimator.animateSplit(mainHorizontalSplit, leftSplitLastLoc, 250);
        }
        isLeftOpen = !isLeftOpen;
    }

    private void updateLeftBar() {
        hierarchyBtn.setSelected(isLeftUpperOpen && isLeftOpen && currentLeftUpperTab.equals("HIERARCHY"));
        commitBtn.setSelected(isLeftUpperOpen && isLeftOpen && currentLeftUpperTab.equals("COMMIT"));
        prBtn.setSelected(isLeftUpperOpen && isLeftOpen && currentLeftUpperTab.equals("PR"));
        projectBtn.setSelected(isProjectOpen && isLeftOpen);
        
        if (!isLeftUpperOpen && !isProjectOpen && isLeftOpen) {
            toggleLeftDrawer();
        } 
        else if ((isLeftUpperOpen || isProjectOpen) && !isLeftOpen) {
            toggleLeftDrawer();
        }
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
        if (simulationLauncher.isRunning()) {
            simulationLauncher.stop();
        }
        new SimulationWindow(simulationLauncher).startSimulation();
    }

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

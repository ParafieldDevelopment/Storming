package com.parafield.storming.ui.windows;

import com.parafield.storming.core.EngineLauncher;
import com.parafield.storming.ui.panels.ConsolePanel;
import com.parafield.storming.ui.panels.SceneViewPanel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainWindow extends JFrame {

    private final ConsolePanel consolePanel;
    private final EngineLauncher engineLauncher;

    public MainWindow() {
        setTitle("Storming Engine - Project: New Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 850);
        setLocationRelativeTo(null);

        // Initialize Core Logic
        consolePanel = new ConsolePanel();
        engineLauncher = new EngineLauncher("../Engine/2D/build/bin/StormingEngine", consolePanel::log);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(30, 30, 30));
        setContentPane(root);

        // 1. Toolbar
        root.add(createToolbar(), BorderLayout.NORTH);

        // 2. Main Center Split
        JSplitPane mainSplit = createSplit(JSplitPane.HORIZONTAL_SPLIT, 280);
        mainSplit.setLeftComponent(createPanel("HIERARCHY", new JTree()));

        JSplitPane rightSplit = createSplit(JSplitPane.HORIZONTAL_SPLIT, 850);
        
        // Tabs for viewport
        JTabbedPane viewportTabs = new JTabbedPane();
        viewportTabs.putClientProperty("JTabbedPane.hasFullBorder", true);
        viewportTabs.addTab("Scene", new SceneViewPanel());
        viewportTabs.addTab("Game", new JPanel());
        rightSplit.setLeftComponent(viewportTabs);

        rightSplit.setRightComponent(createPanel("INSPECTOR", new JTextArea("Select an object...")));
        mainSplit.setRightComponent(rightSplit);

        // 3. Bottom Split (Content | Console)
        JSplitPane bottomSplit = createSplit(JSplitPane.VERTICAL_SPLIT, 600);
        bottomSplit.setTopComponent(mainSplit);
        bottomSplit.setBottomComponent(consolePanel);

        root.add(bottomSplit, BorderLayout.CENTER);
        root.add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JSplitPane createSplit(int orient, int loc) {
        JSplitPane split = new JSplitPane(orient);
        split.setDividerLocation(loc);
        split.setDividerSize(2);
        split.setBorder(null);
        // Correct property name for split pane style
        split.putClientProperty("JSplitPane.style", "thin");
        return split;
    }

    private JPanel createPanel(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(35, 35, 35));
        
        JLabel l = new JLabel(title);
        l.setFont(new Font("Inter", Font.BOLD, 10));
        l.setForeground(new Color(150, 150, 150));
        l.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(38, 38, 38));
        header.add(l, BorderLayout.WEST);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)));
        
        p.add(header, BorderLayout.NORTH);
        p.add(new JScrollPane(content), BorderLayout.CENTER);
        return p;
    }

    private JToolBar createToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)));

        JButton playBtn = new JButton("Play");
        playBtn.putClientProperty("FlatLaf.style", "arc: 20; background: #3c5e3c");
        playBtn.addActionListener(e -> engineLauncher.launch());

        tb.add(Box.createHorizontalGlue());
        tb.add(playBtn);
        tb.add(Box.createHorizontalStrut(5));
        tb.add(new JButton("Stop"));
        tb.add(Box.createHorizontalGlue());
        tb.add(new JLabel("Storming v0.1.0-alpha  "));
        return tb;
    }

    private JPanel createStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(35, 35, 35));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 50)));
        JLabel status = new JLabel("  OpenGL 4.5 | Ready");
        status.setFont(new Font("Inter", Font.PLAIN, 11));
        p.add(status, BorderLayout.WEST);
        return p;
    }
}

package com.parafield.storming;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class EditorApp extends JFrame {

    private JTextArea consoleOutput;

    public EditorApp() {
        setupTheme();
        
        setTitle("Storming Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 850);
        setLocationRelativeTo(null);

        initUI();
    }

    private void setupTheme() {
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
            
            // Modern UI Tweaks (The "IntelliJ" look)
            UIManager.put("Button.arc", 6);
            UIManager.put("Component.arc", 6);
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("Separator.foreground", new Color(60, 60, 60));
            
            // Clean tabs
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.tabSeparatorsFullHeight", true);
            UIManager.put("TabbedPane.selectedBackground", new Color(50, 50, 50));
            
        } catch (Exception ex) {
            System.err.println("Failed to initialize modern theme");
        }
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(30, 30, 30));
        setContentPane(root);

        // 1. Sleek Toolbar
        root.add(createModernToolbar(), BorderLayout.NORTH);

        // 2. Main Layout (Using Split Panes with no borders)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(280);
        mainSplit.setDividerSize(2);
        mainSplit.setBorder(new EmptyBorder(0,0,0,0));
        mainSplit.putClientProperty("FlatLaf.splitPane.style", "thin");

        // Left: Hierarchy (with padding)
        mainSplit.setLeftComponent(createStyledPanel("HIERARCHY", createHierarchyContent()));

        // Right side: Viewport + Inspector
        JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        rightSplit.setDividerLocation(850);
        rightSplit.setDividerSize(2);
        rightSplit.setBorder(new EmptyBorder(0,0,0,0));
        rightSplit.putClientProperty("FlatLaf.splitPane.style", "thin");

        // Center: Tabbed Viewport
        JTabbedPane viewportTabs = new JTabbedPane();
        viewportTabs.putClientProperty("JTabbedPane.hasFullBorder", true);
        viewportTabs.addTab("Scene", createSceneView());
        viewportTabs.addTab("Game", new JPanel());
        rightSplit.setLeftComponent(viewportTabs);

        // Right: Inspector
        rightSplit.setRightComponent(createStyledPanel("INSPECTOR", createInspectorContent()));

        mainSplit.setRightComponent(rightSplit);

        // 3. Bottom Console
        JSplitPane bottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottomSplit.setTopComponent(mainSplit);
        bottomSplit.setBottomComponent(createStyledPanel("CONSOLE", createConsoleContent()));
        bottomSplit.setDividerLocation(600);
        bottomSplit.setDividerSize(2);
        bottomSplit.setBorder(null);

        root.add(bottomSplit, BorderLayout.CENTER);

        // 4. Status Bar
        root.add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JToolBar createModernToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.setMargin(new Insets(5, 10, 5, 10));
        tb.setBackground(new Color(35, 35, 35));
        tb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)));

        JButton playBtn = new JButton("Play");
        playBtn.putClientProperty("FlatLaf.style", "arc: 20; background: #3c5e3c");
        playBtn.addActionListener(e -> launchEngine());

        tb.add(new JButton("File"));
        tb.add(new JButton("Build"));
        tb.add(Box.createHorizontalGlue());
        tb.add(playBtn);
        tb.add(Box.createHorizontalStrut(5));
        tb.add(new JButton("Stop"));
        tb.add(Box.createHorizontalGlue());
        tb.add(new JLabel("Storming Engine v0.1.0-alpha"));
        
        return tb;
    }

    private JPanel createStyledPanel(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(35, 35, 35));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 10));
        titleLabel.setForeground(new Color(150, 150, 150));
        titleLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(38, 38, 38));
        header.add(titleLabel, BorderLayout.WEST);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)));
        
        p.add(header, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private JComponent createHierarchyContent() {
        JTree tree = new JTree();
        tree.setBackground(new Color(30, 30, 30));
        tree.setBorder(new EmptyBorder(5, 5, 5, 5));
        return new JScrollPane(tree);
    }

    private JComponent createInspectorContent() {
        JTextArea area = new JTextArea("Select an object...");
        area.setBackground(new Color(30, 30, 30));
        area.setMargin(new Insets(10, 10, 10, 10));
        return new JScrollPane(area);
    }

    private JComponent createConsoleContent() {
        consoleOutput = new JTextArea("[System] Ready.\n");
        consoleOutput.setBackground(new Color(25, 25, 25));
        consoleOutput.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        consoleOutput.setMargin(new Insets(10, 10, 10, 10));
        return new JScrollPane(consoleOutput);
    }

    private JPanel createSceneView() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 20, 20));
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw a very subtle grid
                g2.setColor(new Color(40, 40, 40, 100));
                for(int i=0; i<getWidth(); i+=40) g2.drawLine(i, 0, i, getHeight());
                for(int i=0; i<getHeight(); i+=40) g2.drawLine(0, i, getWidth(), i);
            }
        };
    }

    private JPanel createStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(35, 35, 35));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 50)));
        
        JLabel status = new JLabel("  OpenGL 4.5 | Ready");
        status.setFont(new Font("Inter", Font.PLAIN, 11));
        status.setBorder(new EmptyBorder(4, 4, 4, 4));
        p.add(status, BorderLayout.WEST);
        return p;
    }

    private void launchEngine() {
        log("Launching Engine...");
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("../Engine/2D/build/bin/StormingEngine");
                pb.redirectErrorStream(true);
                Process p = pb.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String l;
                while ((l = r.readLine()) != null) log("[Engine] " + l);
            } catch (Exception ex) {
                log("[Error] " + ex.getMessage());
            }
        }).start();
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> consoleOutput.append(msg + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Apply theme first
            try {
                UIManager.setLookAndFeel(new FlatMacDarkLaf());
                UIManager.put("Button.arc", 6);
                UIManager.put("Component.arc", 6);
            } catch (Exception ex) {
                System.err.println("Failed to initialize theme");
            }

            // 1. Show Splash Screen
            JWindow splash = new JWindow();
            try {
                ImageIcon originalIcon = new ImageIcon("assets/splashscreen.png");
                Image img = originalIcon.getImage();
                if (img.getWidth(null) > 0) {
                    // Scaling down to 800 width while maintaining aspect ratio
                    int width = 800;
                    int height = (int) (img.getHeight(null) * (800.0 / img.getWidth(null)));
                    Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    
                    JLabel label = new JLabel(new ImageIcon(scaledImg));
                    splash.getContentPane().add(label);
                    splash.pack();
                    splash.setLocationRelativeTo(null);
                    splash.setVisible(true);
                }
            } catch (Exception e) {
                System.err.println("Could not load splash screen: " + e.getMessage());
            }

            // 2. Show WIP Popup
            JOptionPane.showMessageDialog(null, 
                "Storming Engine Rework is currently in early development (WIP).\n" +
                "Many features are not yet implemented.", 
                "Storming Development Branch", 
                JOptionPane.INFORMATION_MESSAGE);

            // 3. Launch Main Editor
            EditorApp app = new EditorApp();
            app.setVisible(true);
            
            // 4. Close Splash
            splash.dispose();
        });
    }
}

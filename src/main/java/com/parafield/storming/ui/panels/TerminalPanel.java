package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TerminalPanel extends JPanel {

    private final JTabbedPane tabbedPane;
    private int sessionCounter = 1;

    public TerminalPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 20));

        // --- 1. Tabbed Pane (The Center) ---
        tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        
        // Right-click to rename or close tabs
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if (index != -1) {
                        showTabContextMenu(e.getComponent(), e.getX(), e.getY(), index);
                    }
                }
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // --- 2. Toolbar (The Right Side) ---
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        toolbar.setBackground(new Color(25, 25, 30));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(60, 60, 60)));
        
        JButton newTabBtn = createToolbarBtn("New Session", Icons.PLUS);
        newTabBtn.addActionListener(e -> addNewSession());
        
        JButton openNativeBtn = createToolbarBtn("External", Icons.TERMINAL);
        openNativeBtn.addActionListener(e -> openExternalTerminal());
        
        JButton clearBtn = createToolbarBtn("Clear", Icons.BRUSH);
        clearBtn.addActionListener(e -> {
            TerminalSession session = getCurrentSession();
            if (session != null) session.clear();
        });

        JButton copyBtn = createToolbarBtn("Copy All", Icons.CLIPBOARD);
        copyBtn.addActionListener(e -> {
            TerminalSession session = getCurrentSession();
            if (session != null) session.copyAll();
        });

        toolbar.add(Box.createVerticalStrut(5));
        toolbar.add(newTabBtn);
        toolbar.addSeparator();
        toolbar.add(openNativeBtn);
        toolbar.add(clearBtn);
        toolbar.add(copyBtn);
        toolbar.add(Box.createVerticalGlue());
        
        add(toolbar, BorderLayout.EAST);

        // Add first session
        addNewSession();
    }

    private JButton createToolbarBtn(String tip, Icon icon) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tip);
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btn.setMaximumSize(new Dimension(32, 32));
        return btn;
    }

    private void addNewSession() {
        TerminalSession session = new TerminalSession();
        tabbedPane.addTab("Session " + (sessionCounter++), session);
        tabbedPane.setSelectedComponent(session);
    }

    private TerminalSession getCurrentSession() {
        Component selected = tabbedPane.getSelectedComponent();
        return (selected instanceof TerminalSession) ? (TerminalSession) selected : null;
    }

    private void showTabContextMenu(Component invoker, int x, int y, int index) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem renameItem = new JMenuItem("Rename Tab...");
        renameItem.addActionListener(e -> {
            String newName = JOptionPane.showInputDialog(this, "Enter tab name:", tabbedPane.getTitleAt(index));
            if (newName != null && !newName.trim().isEmpty()) {
                tabbedPane.setTitleAt(index, newName);
            }
        });
        
        JMenuItem closeItem = new JMenuItem("Close Session");
        closeItem.addActionListener(e -> {
            TerminalSession session = (TerminalSession) tabbedPane.getComponentAt(index);
            session.stop();
            tabbedPane.removeTabAt(index);
        });

        menu.add(renameItem);
        menu.addSeparator();
        menu.add(closeItem);
        menu.show(invoker, x, y);
    }

    private void openExternalTerminal() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                try { new ProcessBuilder("wt").start(); } 
                catch (IOException e) { new ProcessBuilder("start", "powershell").start(); }
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", "-a", "Terminal", ".").start();
            } else {
                String[] terminals = {"kitty", "alacritty", "gnome-terminal", "konsole", "xterm"};
                for (String term : terminals) {
                    try { new ProcessBuilder(term).start(); return; } catch (IOException ignored) {}
                }
            }
        } catch (Exception e) {}
    }

    // --- Inner Class for Individual Session ---
    private static class TerminalSession extends JPanel {
        private final JTextPane terminalArea;
        private final JTextField inputField;
        private Process process;
        private BufferedWriter writer;
        private final List<String> history = new ArrayList<>();
        private int historyIndex = -1;
        private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[([\\d;]*)m");

        public TerminalSession() {
            setLayout(new BorderLayout());
            setBackground(new Color(15, 15, 20));

            terminalArea = new JTextPane();
            terminalArea.setEditable(false);
            terminalArea.setOpaque(false);
            terminalArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
            terminalArea.setMargin(new Insets(10, 15, 10, 15));

            JScrollPane scrollPane = new JScrollPane(terminalArea);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(null);
            add(scrollPane, BorderLayout.CENTER);

            JPanel inputPanel = new JPanel(new BorderLayout());
            inputPanel.setOpaque(false);
            inputPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));
            
            inputField = new JTextField();
            inputField.setOpaque(false);
            inputField.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            inputField.setFont(new Font("JetBrains Mono", Font.BOLD, 13));
            inputField.setForeground(new Color(46, 204, 113));
            inputField.setCaretColor(Color.WHITE);
            
            inputField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) sendCommand();
                    else if (e.getKeyCode() == KeyEvent.VK_UP) navigateHistory(1);
                    else if (e.getKeyCode() == KeyEvent.VK_DOWN) navigateHistory(-1);
                }
            });
            
            JLabel prompt = new JLabel(" $ ");
            prompt.setFont(new Font("JetBrains Mono", Font.BOLD, 13));
            prompt.setForeground(new Color(52, 152, 219));
            inputPanel.add(prompt, BorderLayout.WEST);
            inputPanel.add(inputField, BorderLayout.CENTER);
            add(inputPanel, BorderLayout.SOUTH);

            startShell();
        }

        public void clear() { terminalArea.setText(""); }
        public void copyAll() {
            StringSelection sel = new StringSelection(terminalArea.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
        }
        public void stop() { if (process != null) process.destroy(); }

        private void navigateHistory(int dir) {
            if (history.isEmpty()) return;
            if (dir == 1) { if (historyIndex == -1) historyIndex = history.size() - 1; else if (historyIndex > 0) historyIndex--; }
            else { if (historyIndex != -1) { historyIndex++; if (historyIndex >= history.size()) { historyIndex = -1; inputField.setText(""); return; } } }
            if (historyIndex != -1) inputField.setText(history.get(historyIndex));
        }

        private void startShell() {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb = os.contains("win") ? new ProcessBuilder("powershell.exe", "-NoLogo") : new ProcessBuilder(System.getenv("SHELL") != null ? System.getenv("SHELL") : "/bin/bash", "-i");
                if (!os.contains("win")) pb.environment().put("TERM", "dumb");
                pb.redirectErrorStream(true);
                process = pb.start();
                writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
                new Thread(() -> {
                    try (InputStreamReader isr = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)) {
                        char[] buffer = new char[1024]; int n;
                        while ((n = isr.read(buffer)) != -1) appendAnsiStyledText(new String(buffer, 0, n));
                    } catch (IOException ignored) {}
                }).start();
            } catch (IOException e) { appendAnsiStyledText("[Error] " + e.getMessage() + "\n"); }
        }

        private void sendCommand() {
            String cmd = inputField.getText(); if (cmd.isEmpty()) return;
            history.add(cmd); historyIndex = -1;
            try { writer.write(cmd + "\n"); writer.flush(); inputField.setText(""); }
            catch (IOException e) { appendAnsiStyledText("[Error] " + e.getMessage() + "\n"); }
        }

        private void appendAnsiStyledText(String text) {
            SwingUtilities.invokeLater(() -> {
                StyledDocument doc = terminalArea.getStyledDocument();
                Matcher m = ANSI_PATTERN.matcher(text);
                int start = 0; Color current = new Color(200, 200, 200);
                try {
                    while (m.find()) {
                        if (m.start() > start) insertText(doc, text.substring(start, m.start()), current);
                        current = parseAnsiColor(m.group(1), current);
                        start = m.end();
                    }
                    if (start < text.length()) insertText(doc, text.substring(start), current);
                    terminalArea.setCaretPosition(doc.getLength());
                } catch (Exception ignored) {}
            });
        }

        private Color parseAnsiColor(String code, Color def) {
            if (code == null || code.isEmpty() || code.equals("0")) return new Color(200, 200, 200);
            return switch (code) {
                case "31" -> new Color(231, 76, 60); case "32" -> new Color(46, 204, 113);
                case "33" -> new Color(241, 196, 15); case "34" -> new Color(52, 152, 219);
                case "35" -> new Color(155, 89, 182); case "36" -> new Color(26, 188, 156);
                default -> def;
            };
        }

        private void insertText(StyledDocument doc, String text, Color color) throws BadLocationException {
            SimpleAttributeSet style = new SimpleAttributeSet();
            StyleConstants.setForeground(style, color);
            doc.insertString(doc.getLength(), text, style);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(15, 15, 20)); g.fillRect(0, 0, getWidth(), getHeight());
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.06f));
            int size = 120;
            if (Icons.TERMINAL_80 != null) Icons.TERMINAL_80.paintIcon(this, g2, getWidth() - size - 40, (getHeight() - size) / 2);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

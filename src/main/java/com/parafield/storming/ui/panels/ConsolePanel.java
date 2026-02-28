package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConsolePanel extends JPanel {

    private final JTextPane consoleOutput;
    private final List<LogEntry> logHistory = new ArrayList<>();
    private final JTextField searchField;
    
    // Filter states
    private boolean showInfo = true;
    private boolean showWarn = true;
    private boolean showError = true;
    private String searchText = "";

    // Custom Colors
    private static final Color COLOR_SYSTEM = new Color(52, 152, 219);  // Blue
    private static final Color COLOR_ENGINE = new Color(230, 126, 34);  // Orange
    private static final Color COLOR_ERROR  = new Color(231, 76, 60);   // Red
    private static final Color COLOR_WARN   = new Color(241, 196, 15);  // Yellow
    private static final Color COLOR_INFO   = Color.LIGHT_GRAY;

    private enum LogType { SYSTEM, ENGINE, ERROR, WARNING, INFO }
    private record LogEntry(String message, LogType type) {}

    public ConsolePanel() {
        setLayout(new BorderLayout());

        // --- 1. Toolbar ---
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
        
        JButton clearBtn = new JButton("Clear");
        clearBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        clearBtn.addActionListener(e -> {
            logHistory.clear();
            refreshConsole();
        });
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(clearBtn);
        toolbar.addSeparator();

        toolbar.add(createFilterBtn("Info", true, b -> { showInfo = b; refreshConsole(); }));
        toolbar.add(createFilterBtn("Warnings", true, b -> { showWarn = b; refreshConsole(); }));
        toolbar.add(createFilterBtn("Errors", true, b -> { showError = b; refreshConsole(); }));
        
        toolbar.add(Box.createHorizontalGlue());
        
        // Extended Search Field
        searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Filter console output...");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Icons.SEARCH);
        searchField.setPreferredSize(new Dimension(350, 26));
        searchField.setMaximumSize(new Dimension(500, 26));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { updateSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { updateSearch(); }
            private void updateSearch() {
                searchText = searchField.getText().toLowerCase();
                refreshConsole();
            }
        });
        toolbar.add(searchField);
        toolbar.add(Box.createHorizontalStrut(10));
        
        add(toolbar, BorderLayout.NORTH);

        // --- 2. Console Output ---
        consoleOutput = new JTextPane();
        consoleOutput.setEditable(false);
        consoleOutput.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 2%)");
        consoleOutput.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        consoleOutput.setMargin(new Insets(5, 10, 5, 10));
        
        JScrollPane scrollPane = new JScrollPane(consoleOutput);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        
        log("[System] Console Initialized.");
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        // Draw subtle watermark icon in the bottom right
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
        
        int size = 80;
        int x = getWidth() - size - 25;
        int y = getHeight() - size - 25;
        if (Icons.CONSOLE_80 != null) {
            Icons.CONSOLE_80.paintIcon(this, g2, x, y);
        }
        g2.dispose();
    }

    private JToggleButton createFilterBtn(String text, boolean selected, java.util.function.Consumer<Boolean> onToggle) {
        JToggleButton btn = new JToggleButton(text);
        btn.setSelected(selected);
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btn.setFont(new Font("Inter", Font.PLAIN, 11));
        btn.addActionListener(e -> onToggle.accept(btn.isSelected()));
        return btn;
    }

    public void log(String msg) {
        LogType type = LogType.INFO;
        if (msg.startsWith("[System]")) type = LogType.SYSTEM;
        else if (msg.startsWith("[Engine]")) type = LogType.ENGINE;
        else if (msg.startsWith("[Error]") || msg.toLowerCase().contains("error")) type = LogType.ERROR;
        else if (msg.startsWith("[Warning]") || msg.toLowerCase().contains("warning")) type = LogType.WARNING;

        LogEntry entry = new LogEntry(msg, type);
        logHistory.add(entry);

        if (shouldDisplay(entry)) {
            appendEntryToUI(entry);
        }
    }

    private boolean shouldDisplay(LogEntry entry) {
        boolean typeMatch = switch (entry.type()) {
            case INFO, SYSTEM, ENGINE -> showInfo;
            case WARNING -> showWarn;
            case ERROR -> showError;
        };
        
        if (!typeMatch) return false;
        
        if (searchText.isEmpty()) return true;
        return entry.message().toLowerCase().contains(searchText);
    }

    private void refreshConsole() {
        consoleOutput.setText("");
        for (LogEntry entry : logHistory) {
            if (shouldDisplay(entry)) {
                appendEntryToUI(entry);
            }
        }
    }

    private void appendEntryToUI(LogEntry entry) {
        SwingUtilities.invokeLater(() -> {
            String msg = entry.message();
            LogType type = entry.type();

            switch (type) {
                case SYSTEM -> {
                    appendStyledText("[System]", COLOR_SYSTEM);
                    appendStyledText(" " + msg.substring(8).trim() + "\n", COLOR_INFO);
                }
                case ENGINE -> {
                    appendStyledText("[Engine]", COLOR_ENGINE);
                    appendStyledText(" " + msg.substring(8).trim() + "\n", COLOR_INFO);
                }
                case ERROR -> appendStyledText(msg + "\n", COLOR_ERROR);
                case WARNING -> appendStyledText(msg + "\n", COLOR_WARN);
                default -> appendStyledText(msg + "\n", COLOR_INFO);
            }
            consoleOutput.setCaretPosition(consoleOutput.getDocument().getLength());
        });
    }

    private void appendStyledText(String text, Color color) {
        StyledDocument doc = consoleOutput.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);
        
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}

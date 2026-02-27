package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class ConsolePanel extends JPanel {

    private final JTextPane consoleOutput;
    
    // Custom Colors
    private static final Color COLOR_SYSTEM = new Color(52, 152, 219);  // Blue
    private static final Color COLOR_ENGINE = new Color(230, 126, 34);  // Orange
    private static final Color COLOR_ERROR  = new Color(231, 76, 60);   // Red
    private static final Color COLOR_WARN   = new Color(241, 196, 15);  // Yellow
    private static final Color COLOR_INFO   = Color.WHITE;

    public ConsolePanel() {
        setLayout(new BorderLayout());

        consoleOutput = new JTextPane();
        consoleOutput.setEditable(false);
        consoleOutput.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 2%)");
        consoleOutput.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        consoleOutput.setMargin(new Insets(10, 10, 10, 10));
        
        add(new JScrollPane(consoleOutput), BorderLayout.CENTER);
        
        log("[System] Ready.");
    }

    public void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.startsWith("[System]")) {
                appendStyledText("[System]", COLOR_SYSTEM);
                appendStyledText(" " + msg.substring(8).trim() + "\n", COLOR_INFO);
            } else if (msg.startsWith("[Engine]")) {
                appendStyledText("[Engine]", COLOR_ENGINE);
                appendStyledText(" " + msg.substring(8).trim() + "\n", COLOR_INFO);
            } else if (msg.startsWith("[Error]") || msg.toLowerCase().contains("error")) {
                appendStyledText(msg + "\n", COLOR_ERROR);
            } else if (msg.startsWith("[Warning]") || msg.toLowerCase().contains("warning")) {
                appendStyledText(msg + "\n", COLOR_WARN);
            } else {
                appendStyledText(msg + "\n", COLOR_INFO);
            }
            consoleOutput.setCaretPosition(consoleOutput.getDocument().getLength());
        });
    }

    private void appendStyledText(String text, Color color) {
        StyledDocument doc = consoleOutput.getStyledDocument();
        Style style = consoleOutput.addStyle("LogStyle", null);
        StyleConstants.setForeground(style, color);
        
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}

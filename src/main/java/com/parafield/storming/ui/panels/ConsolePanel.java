package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;

public class ConsolePanel extends JPanel {

    private final JTextArea consoleOutput;

    public ConsolePanel() {
        setLayout(new BorderLayout());

        consoleOutput = new JTextArea("[System] Ready. ");
        consoleOutput.setEditable(false);
        consoleOutput.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 2%)");
        consoleOutput.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        consoleOutput.setMargin(new Insets(10, 10, 10, 10));
        
        add(new JScrollPane(consoleOutput), BorderLayout.CENTER);
    }

    public void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            consoleOutput.append(msg + " ");
            consoleOutput.setCaretPosition(consoleOutput.getDocument().getLength());
        });
    }
}

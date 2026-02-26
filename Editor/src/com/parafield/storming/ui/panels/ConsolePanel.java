package com.parafield.storming.ui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ConsolePanel extends JPanel {

    private final JTextArea consoleOutput;

    public ConsolePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(35, 35, 35));

        JLabel titleLabel = new JLabel("  CONSOLE");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 10));
        titleLabel.setForeground(new Color(150, 150, 150));
        titleLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(38, 38, 38));
        header.add(titleLabel, BorderLayout.WEST);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)));
        
        consoleOutput = new JTextArea("[System] Ready. ");
        consoleOutput.setEditable(false);
        consoleOutput.setBackground(new Color(25, 25, 25));
        consoleOutput.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        consoleOutput.setMargin(new Insets(10, 10, 10, 10));
        
        add(header, BorderLayout.NORTH);
        add(new JScrollPane(consoleOutput), BorderLayout.CENTER);
    }

    public void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            consoleOutput.append(msg + " ");
            consoleOutput.setCaretPosition(consoleOutput.getDocument().getLength());
        });
    }
}

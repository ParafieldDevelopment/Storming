package com.parafield.storming.ui.panels;

import javax.swing.*;
import java.awt.*;

public class AnalyzerPanel extends JPanel {
    public AnalyzerPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Script & Asset Analyzer", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}

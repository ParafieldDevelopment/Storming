package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.parafield.storming.Icons;
import com.parafield.storming.ui.utils.UIUtils;
import com.parafield.storming.ui.windows.MainWindow;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A professional, block-based property inspector.
 * Groups properties into distinct, rounded cards to prevent a "stretched" appearance.
 */
public class InspectorPanel extends JPanel {

    private final JPanel scrollContent;
    private int currentEntityId = -1;

    public InspectorPanel() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));

        scrollContent = new JPanel(new GridBagLayout());
        scrollContent.setOpaque(false);

        JScrollPane scroll = new JScrollPane(scrollContent);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        showEmptyState();
    }

    private void showEmptyState() {
        scrollContent.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel label = new JLabel("No object selected");
        label.setFont(UIUtils.getFont(Font.PLAIN, 11f));
        label.setForeground(UIManager.getColor("Label.disabledForeground"));
        scrollContent.add(label, gbc);
        scrollContent.revalidate();
        scrollContent.repaint();
    }

    public void updateDetails(JsonObject details) {
        SwingUtilities.invokeLater(() -> {
            scrollContent.removeAll();
            currentEntityId = details.get("id").getAsInt();

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(12, 12, 8, 12);

            // 1. Header Block (Name & Icon)
            JPanel header = new JPanel(new BorderLayout(10, 0));
            header.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 3%); arc: 12");
            header.setBorder(new EmptyBorder(8, 12, 8, 12));
            
            JLabel icon = new JLabel(Icons.EXE_ICON);
            JTextField nameField = new JTextField(details.get("tag").getAsString());
            nameField.setFont(UIUtils.getFont(Font.BOLD, 13f));
            nameField.putClientProperty(FlatClientProperties.STYLE, "background: #00000000; borderWidth: 0; focusWidth: 0;");
            
            header.add(icon, BorderLayout.WEST);
            header.add(nameField, BorderLayout.CENTER);
            
            scrollContent.add(header, gbc);
            gbc.gridy++;
            gbc.insets = new Insets(4, 12, 4, 12); // Tighter spacing for components

            // 2. Components
            if (details.has("components")) {
                JsonObject components = details.getAsJsonObject("components");
                
                if (components.has("Transform")) {
                    scrollContent.add(createSection("Transform", createTransformUI(components.getAsJsonObject("Transform"))), gbc);
                    gbc.gridy++;
                }
                
                if (components.has("SpriteRenderer")) {
                    scrollContent.add(createSection("Sprite Renderer", createSpriteUI(components.getAsJsonObject("SpriteRenderer"))), gbc);
                    gbc.gridy++;
                }
            }

            // 3. The "Anti-Stretch" Spacer
            gbc.weighty = 1.0;
            scrollContent.add(Box.createGlue(), gbc);

            scrollContent.revalidate();
            scrollContent.repaint();
        });
    }

    private JPanel createSection(String title, JPanel body) {
        JPanel section = new JPanel(new BorderLayout());
        section.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 1.5%); arc: 10");
        
        JLabel header = new JLabel(title);
        header.setFont(UIUtils.getFont(Font.BOLD, 10f));
        header.setForeground(new Color(150, 150, 155));
        header.setBorder(new EmptyBorder(8, 12, 4, 12));
        
        section.add(header, BorderLayout.NORTH);
        section.add(body, BorderLayout.CENTER);
        return section;
    }

    private JPanel createTransformUI(JsonObject data) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 12, 10, 12));

        addPropertyRow(p, 0, "Position", createVector3Inputs(data.getAsJsonArray("translation"), "transform", "translation"));
        addPropertyRow(p, 1, "Rotation", createVector3Inputs(data.getAsJsonArray("rotation"), "transform", "rotation"));
        addPropertyRow(p, 2, "Scale", createVector3Inputs(data.getAsJsonArray("scale"), "transform", "scale"));

        return p;
    }

    private JPanel createSpriteUI(JsonObject data) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 12, 10, 12));

        JsonArray colorArr = data.getAsJsonArray("color");
        Color initialColor = new Color(colorArr.get(0).getAsFloat(), colorArr.get(1).getAsFloat(), colorArr.get(2).getAsFloat());
        
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(80, 18));
        colorBox.setBackground(initialColor);
        colorBox.setBorder(BorderFactory.createLineBorder(new Color(255,255,255,30)));
        colorBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        colorBox.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                Color newColor = JColorChooser.showDialog(InspectorPanel.this, "Select Color", initialColor);
                if (newColor != null) {
                    colorBox.setBackground(newColor);
                    updateColor(newColor);
                }
            }
        });

        addPropertyRow(p, 0, "Color", colorBox);
        return p;
    }

    private void addPropertyRow(JPanel parent, int row, String label, JComponent input) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 10);
        
        JLabel l = new JLabel(label);
        l.setFont(UIUtils.getFont(Font.PLAIN, 11f));
        l.setPreferredSize(new Dimension(70, 22));
        parent.add(l, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 0, 2, 0);
        parent.add(input, gbc);
    }

    private JPanel createVector3Inputs(JsonArray values, String comp, String field) {
        JPanel p = new JPanel(new GridLayout(1, 3, 4, 0));
        p.setOpaque(false);
        Color[] axisColors = {new Color(231, 76, 60), new Color(46, 204, 113), new Color(52, 152, 219)};
        
        for (int i = 0; i < 3; i++) {
            final int index = i;
            JTextField f = new JTextField(String.format("%.2f", values.get(index).getAsFloat()));
            f.setFont(UIUtils.getFont(Font.PLAIN, 10f));
            f.setHorizontalAlignment(JTextField.LEFT);
            f.setPreferredSize(new Dimension(0, 20));
            f.putClientProperty(FlatClientProperties.STYLE, "padding: 0,2,0,2; borderWidth: 0; focusWidth: 0; background: darken($Panel.background, 5%)");
            f.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, axisColors[i]));
            
            f.addActionListener(e -> applyValue(f, comp, field, index));
            f.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusLost(java.awt.event.FocusEvent e) { applyValue(f, comp, field, index); }
            });
            p.add(f);
        }
        return p;
    }

    private void applyValue(JTextField f, String component, String field, int index) {
        try {
            float val = Float.parseFloat(f.getText());
            String cmd = String.format("{\"type\":\"command\",\"action\":\"update_component\",\"id\":%d,\"component\":\"%s\",\"field\":\"%s\",\"index\":%d,\"value\":%.4f}",
                    currentEntityId, component, field, index, val);
            MainWindow.getInstance().getEngineLauncher().sendCommand(cmd);
        } catch (NumberFormatException ignored) {}
    }
    
    private void updateColor(Color c) {
        String cmd = String.format("{\"type\":\"command\",\"action\":\"update_component\",\"id\":%d,\"component\":\"spriterenderer\",\"field\":\"color\",\"r\":%.3f,\"g\":%.3f,\"b\":%.3f,\"a\":1.0}",
                currentEntityId, c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f);
        MainWindow.getInstance().getEngineLauncher().sendCommand(cmd);
    }
}

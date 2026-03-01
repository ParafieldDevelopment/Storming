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

/**
 * Provides a property editor for the currently selected object in the scene.
 * Dynamically builds the UI based on components received from the engine.
 */
public class InspectorPanel extends JPanel {

    private final JPanel content;
    private int currentEntityId = -1;

    /**
     * Constructs an InspectorPanel with a prompt to select an object.
     */
    public InspectorPanel() {
        setLayout(new BorderLayout());
        
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        
        showEmptyState();
        
        add(new JScrollPane(content), BorderLayout.CENTER);
    }

    private void showEmptyState() {
        content.removeAll();
        content.add(Box.createVerticalGlue());
        JLabel label = new JLabel("Select an object to inspect", SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(UIManager.getColor("Label.disabledForeground"));
        content.add(label);
        content.add(Box.createVerticalGlue());
        content.revalidate();
        content.repaint();
    }

    /**
     * Updates the inspector with the details of a selected entity.
     * @param details JsonObject containing entity ID and component data.
     */
    public void updateDetails(JsonObject details) {
        SwingUtilities.invokeLater(() -> {
            content.removeAll();
            currentEntityId = details.get("id").getAsInt();
            
            // Header: Icon + Name
            JPanel header = new JPanel(new BorderLayout(10, 0));
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            JLabel icon = new JLabel(Icons.EXE_ICON);
            JTextField nameField = new JTextField(details.get("tag").getAsString());
            nameField.putClientProperty(FlatClientProperties.STYLE, "background: #00000000; borderWidth: 0; font: bold;");
            
            header.add(icon, BorderLayout.WEST);
            header.add(nameField, BorderLayout.CENTER);
            content.add(header);
            
            content.add(new JSeparator());

            // Components
            if (details.has("components")) {
                JsonObject components = details.getAsJsonObject("components");
                
                if (components.has("Transform")) {
                    addComponentPanel("Transform", createTransformUI(components.getAsJsonObject("Transform")));
                }
                
                if (components.has("SpriteRenderer")) {
                    addComponentPanel("Sprite Renderer", createSpriteUI(components.getAsJsonObject("SpriteRenderer")));
                }
            }

            content.add(Box.createVerticalGlue());
            content.revalidate();
            content.repaint();
        });
    }

    private void addComponentPanel(String title, JPanel componentUI) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UIUtils.getFont(Font.BOLD, 10f));
        titleLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        titleLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        wrapper.add(titleLabel, BorderLayout.NORTH);
        wrapper.add(componentUI, BorderLayout.CENTER);
        
        content.add(wrapper);
        content.add(new JSeparator());
    }

    private JPanel createTransformUI(JsonObject data) {
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 5));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(5, 10, 5, 10));

        p.add(createVector3Row("Position", data.getAsJsonArray("translation"), "transform", "translation"));
        p.add(createVector3Row("Rotation", data.getAsJsonArray("rotation"), "transform", "rotation"));
        p.add(createVector3Row("Scale", data.getAsJsonArray("scale"), "transform", "scale"));

        return p;
    }

    private JPanel createSpriteUI(JsonObject data) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(5, 10, 5, 10));

        p.add(new JLabel("Color: "));
        JsonArray color = data.getAsJsonArray("color");
        // Simplified color preview
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(40, 20));
        colorBox.setBackground(new Color(color.get(0).getAsFloat(), color.get(1).getAsFloat(), color.get(2).getAsFloat()));
        p.add(colorBox);

        return p;
    }

    private JPanel createVector3Row(String label, JsonArray values, String componentName, String fieldName) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(60, 20));
        p.add(l, BorderLayout.WEST);

        JPanel inputs = new JPanel(new GridLayout(1, 3, 5, 0));
        inputs.setOpaque(false);
        
        String[] axis = {"X", "Y", "Z"};
        for (int i = 0; i < 3; i++) {
            final int index = i;
            JTextField f = new JTextField(String.format("%.2f", values.get(i).getAsFloat()));
            f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, axis[i]);
            f.setHorizontalAlignment(JTextField.CENTER);
            
            f.addActionListener(e -> {
                try {
                    float val = Float.parseFloat(f.getText());
                    updateComponentValue(componentName, fieldName, index, val);
                } catch (NumberFormatException ignored) {}
            });
            
            inputs.add(f);
        }
        
        p.add(inputs, BorderLayout.CENTER);
        return p;
    }

    private void updateComponentValue(String component, String field, int index, float value) {
        String cmd = String.format("{\"type\":\"command\",\"action\":\"update_component\",\"id\":%d,\"component\":\"%s\",\"field\":\"%s\",\"index\":%d,\"value\":%.4f}",
                currentEntityId, component, field, index, value);
        MainWindow.getInstance().getEngineLauncher().sendCommand(cmd);
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (content.getComponentCount() == 2) { // Just Empty state
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
            
            int size = 80;
            int x = getWidth() - size - 25;
            int y = getHeight() - size - 25;
            if (Icons.SEARCH_80 != null) {
                Icons.SEARCH_80.paintIcon(this, g2, x, y);
            }
            g2.dispose();
        }
    }
}

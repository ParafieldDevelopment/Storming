package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.parafield.storming.Icons;
import com.parafield.storming.ui.utils.UIUtils;
import com.parafield.storming.ui.windows.MainWindow;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * A professional, high-utility property inspector.
 * Features collapsible blocks, property resets, and specialized 2D rotation controls.
 */
public class InspectorPanel extends JPanel {

    private final JPanel scrollContent;
    private int currentEntityId = -1;
    private final Map<String, Boolean> collapsedStates = new HashMap<>();
    private static final int LABEL_WIDTH = 70;

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

            // 1. Header Card
            JPanel header = new JPanel(new BorderLayout(10, 0));
            header.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 4%); arc: 12");
            header.setBorder(new EmptyBorder(8, 12, 8, 12));
            header.add(new JLabel(Icons.EXE_ICON), BorderLayout.WEST);
            
            JTextField nameField = new JTextField(details.get("tag").getAsString());
            nameField.setFont(UIUtils.getFont(Font.BOLD, 13f));
            nameField.putClientProperty(FlatClientProperties.STYLE, "background: #00000000; borderWidth: 0; focusWidth: 0;");
            header.add(nameField, BorderLayout.CENTER);
            
            scrollContent.add(header, gbc);
            gbc.gridy++;
            gbc.insets = new Insets(4, 12, 4, 12);

            // 2. Components
            if (details.has("components")) {
                JsonObject components = details.getAsJsonObject("components");
                if (components.has("Transform")) {
                    scrollContent.add(createCollapsibleSection("Transform", createTransformUI(components.getAsJsonObject("Transform"))), gbc);
                    gbc.gridy++;
                }
                if (components.has("SpriteRenderer")) {
                    scrollContent.add(createCollapsibleSection("Sprite Renderer", createSpriteUI(components.getAsJsonObject("SpriteRenderer"))), gbc);
                    gbc.gridy++;
                }
                if (components.has("Script")) {
                    scrollContent.add(createCollapsibleSection("Script", createScriptUI(components.getAsJsonObject("Script"))), gbc);
                    gbc.gridy++;
                }
            }

            // 3. Add Component Button
            gbc.insets = new Insets(15, 40, 15, 40);
            JButton addCompBtn = new JButton("Add Component", Icons.PLUS);
            addCompBtn.putClientProperty(FlatClientProperties.STYLE, "background: #34495e; foreground: #ffffff; arc: 20");
            addCompBtn.setFont(UIUtils.getFont(Font.BOLD, 11f));
            addCompBtn.addActionListener(e -> showAddComponentMenu(addCompBtn));
            scrollContent.add(addCompBtn, gbc);
            gbc.gridy++;

            gbc.weighty = 1.0;
            scrollContent.add(Box.createGlue(), gbc);
            scrollContent.revalidate();
            scrollContent.repaint();
        });
    }

    private void showAddComponentMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem("Script")).addActionListener(e -> 
            MainWindow.getInstance().getEngineLauncher().sendCommand(
                String.format("{\"type\":\"command\",\"action\":\"add_component\",\"id\":%d,\"component\":\"script\"}", currentEntityId)
            )
        );
        menu.show(invoker, 0, invoker.getHeight());
    }

    private JPanel createCollapsibleSection(String title, JPanel body) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        boolean isCollapsed = collapsedStates.getOrDefault(title, false);

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 2%); arc: 10");
        header.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UIUtils.getFont(Font.BOLD, 10f));
        titleLabel.setForeground(new Color(180, 180, 185));
        header.add(titleLabel, BorderLayout.WEST);
        
        JLabel arrow = new JLabel(isCollapsed ? "▶" : "▼");
        arrow.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        header.add(arrow, BorderLayout.EAST);

        header.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                collapsedStates.put(title, !isCollapsed);
                MainWindow.getInstance().getEngineLauncher().sendCommand("{\"type\":\"command\",\"action\":\"select_entity\",\"id\":" + currentEntityId + "}");
            }
        });

        container.add(header, BorderLayout.NORTH);
        if (!isCollapsed) {
            body.setBackground(UIManager.getColor("Panel.background"));
            body.setBorder(new EmptyBorder(8, 0, 12, 0));
            container.add(body, BorderLayout.CENTER);
        }
        return container;
    }

    private JPanel createTransformUI(JsonObject data) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        
        addPropertyRow(p, 0, "Position", createVector3Inputs(data.getAsJsonArray("translation"), "transform", "translation"));
        addPropertyRow(p, 1, "Rotation", createRotationSlider(data.getAsJsonArray("rotation")));
        addPropertyRow(p, 2, "Scale", createVector3Inputs(data.getAsJsonArray("scale"), "transform", "scale"));
        
        return p;
    }

    private JPanel createRotationSlider(JsonArray rotation) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);

        float currentRad = rotation.get(2).getAsFloat();
        int currentDeg = (int) Math.toDegrees(currentRad) % 360;
        if (currentDeg < 0) currentDeg += 360;

        JTextField field = new JTextField(String.valueOf(currentDeg));
        field.setPreferredSize(new Dimension(45, 22));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setFont(UIUtils.getFont(Font.PLAIN, 10f));
        field.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 5%); borderWidth: 0;");

        JSlider slider = new JSlider(0, 360, currentDeg);
        slider.setOpaque(false);
        slider.setPreferredSize(new Dimension(100, 20));
        slider.putClientProperty(FlatClientProperties.STYLE, "thumbWidth: 12; trackHeight: 2;");

        slider.addChangeListener(e -> {
            if (slider.getValueIsAdjusting()) {
                field.setText(String.valueOf(slider.getValue()));
                updateRotation(slider.getValue());
            }
        });

        field.addActionListener(e -> {
            try {
                int val = Integer.parseInt(field.getText());
                slider.setValue(val);
                updateRotation(val);
            } catch (NumberFormatException ignored) {}
        });

        p.add(field, BorderLayout.WEST);
        p.add(slider, BorderLayout.CENTER);
        return p;
    }

    private void updateRotation(int degrees) {
        float radians = (float) Math.toRadians(degrees);
        String cmd = String.format("{\"type\":\"command\",\"action\":\"update_component\",\"id\":%d,\"component\":\"transform\",\"field\":\"rotation\",\"index\":2,\"value\":%.4f}",
                currentEntityId, radians);
        MainWindow.getInstance().getEngineLauncher().sendCommand(cmd);
    }

    private JPanel createSpriteUI(JsonObject data) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
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

        String texPath = data.has("texture") ? data.get("texture").getAsString() : "";
        String texName = texPath.isEmpty() ? "None (Texture2D)" : new java.io.File(texPath).getName();
        
        JLabel texSlot = new JLabel(texName, Icons.GRID, SwingConstants.LEFT);
        texSlot.setFont(UIUtils.getFont(Font.PLAIN, 10f));
        texSlot.setPreferredSize(new Dimension(120, 22));
        texSlot.setOpaque(true);
        texSlot.setBackground(new Color(30, 30, 35));
        texSlot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 105, 50), 1, true),
            BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));

        texSlot.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor);
            }
            @Override
            public boolean importData(TransferSupport support) {
                try {
                    String path = (String) support.getTransferable().getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor);
                    if (path.toLowerCase().endsWith(".png") || path.toLowerCase().endsWith(".jpg")) {
                        updateTexture(path);
                        return true;
                    }
                } catch (Exception ignored) {}
                return false;
            }
        });

        addPropertyRow(p, 0, "Color", colorBox);
        addPropertyRow(p, 1, "Texture", texSlot);
        return p;
    }

    private JPanel createScriptUI(JsonObject data) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        
        String path = data.has("path") ? data.get("path").getAsString() : "";
        String fileName = path.isEmpty() ? "None (Lua)" : new java.io.File(path).getName();

        JPanel fileBox = new JPanel(new BorderLayout(5, 0));
        fileBox.setOpaque(false);
        
        JTextField pathField = new JTextField(fileName);
        pathField.setEditable(false);
        pathField.setFont(UIUtils.getFont(Font.PLAIN, 10f));
        pathField.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 5%); borderWidth: 0;");
        
        JButton pickBtn = new JButton("...");
        pickBtn.setPreferredSize(new Dimension(24, 22));
        pickBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(new java.io.File("Plugins")); // Default to plugins dir
            fc.setFileFilter(new FileNameExtensionFilter("Lua Scripts (*.lua)", "lua"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                updateScript(fc.getSelectedFile().getAbsolutePath());
            }
        });

        fileBox.add(pathField, BorderLayout.CENTER);
        fileBox.add(pickBtn, BorderLayout.EAST);

        // Drag & Drop for Scripts
        pathField.setTransferHandler(new TransferHandler() {
            @Override public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor);
            }
            @Override public boolean importData(TransferSupport support) {
                try {
                    String p = (String) support.getTransferable().getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor);
                    if (p.endsWith(".lua")) { updateScript(p); return true; }
                } catch (Exception ignored) {}
                return false;
            }
        });

        addPropertyRow(p, 0, "Source", fileBox);
        return p;
    }

    private void updateScript(String path) {
        String cmd = String.format("{\"type\":\"command\",\"action\":\"update_component\",\"id\":%d,\"component\":\"script\",\"field\":\"path\",\"path\":\"%s\"}",
                currentEntityId, path.replace("\\", "/"));
        MainWindow.getInstance().getEngineLauncher().sendCommand(cmd);
    }

    private void updateTexture(String path) {
        String cmd = String.format("{\"type\":\"command\",\"action\":\"update_component\",\"id\":%d,\"component\":\"spriterenderer\",\"field\":\"texture\",\"path\":\"%s\"}",
                currentEntityId, path.replace("\\", "/"));
        MainWindow.getInstance().getEngineLauncher().sendCommand(cmd);
    }

    private void addPropertyRow(JPanel parent, int row, String label, JComponent input) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 12, 2, 8);
        
        JLabel l = new JLabel(label);
        l.setFont(UIUtils.getFont(Font.PLAIN, 11f));
        l.setPreferredSize(new Dimension(LABEL_WIDTH, 22));
        parent.add(l, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 0, 2, 5);
        parent.add(input, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 12);
        JButton resetBtn = new JButton(Icons.RESTART);
        resetBtn.setPreferredSize(new Dimension(18, 18));
        resetBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        parent.add(resetBtn, gbc);
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

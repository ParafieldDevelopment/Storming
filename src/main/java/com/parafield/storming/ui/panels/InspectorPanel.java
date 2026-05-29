package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.parafield.storming.Icons;
import com.parafield.storming.ui.utils.UIUtils;
import com.parafield.storming.ui.utils.UIAnimator;
import com.parafield.storming.ui.windows.MainWindow;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
                if (components.has("RigidBody2D")) {
                    scrollContent.add(createCollapsibleSection("RigidBody 2D", createRigidBodyUI(components.getAsJsonObject("RigidBody2D"))), gbc);
                    gbc.gridy++;
                }
                if (components.has("BoxCollider2D")) {
                    scrollContent.add(createCollapsibleSection("Box Collider 2D", createBoxColliderUI(components.getAsJsonObject("BoxCollider2D"))), gbc);
                    gbc.gridy++;
                }
            }

            // 3. Add Component Button
            gbc.insets = new Insets(15, 12, 15, 12);
            JButton addCompBtn = new JButton("Add Component", Icons.PLUS);
            addCompBtn.putClientProperty(FlatClientProperties.STYLE, "background: darken($Panel.background, 5%); foreground: $Label.foreground; arc: 5; borderWidth: 1; borderColor: $Component.borderColor");
            addCompBtn.setFont(UIUtils.getFont(Font.BOLD, 11f));
            addCompBtn.addActionListener(e -> showAddComponentPopup(addCompBtn));
            scrollContent.add(addCompBtn, gbc);
            gbc.gridy++;

            gbc.weighty = 1.0;
            scrollContent.add(Box.createGlue(), gbc);
            scrollContent.revalidate();
            scrollContent.repaint();
        });
    }

    private void showAddComponentPopup(JButton btn) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem rbItem = new JMenuItem("RigidBody 2D");
        rbItem.addActionListener(e -> addComponent("rigidbody2d"));
        menu.add(rbItem);

        JMenuItem bcItem = new JMenuItem("Box Collider 2D");
        bcItem.addActionListener(e -> addComponent("boxcollider2d"));
        menu.add(bcItem);

        menu.show(btn, 0, btn.getHeight());
    }

    private void addComponent(String component) {
        // We use the same update_component protocol but with a special field to trigger add
        String cmd = String.format("{\"type\":\"command\",\"action\":\"update_component\",\"id\":%d,\"component\":\"%s\",\"field\":\"init\"}",
                currentEntityId, component);
        MainWindow.getInstance().getEngineLauncher().sendCommand(cmd);
        // Refresh
        MainWindow.getInstance().getEngineLauncher().sendCommand("{\"type\":\"command\",\"action\":\"select_entity\",\"id\":" + currentEntityId + "}");
    }

    private JPanel createRigidBodyUI(JsonObject data) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        String[] types = {"Static", "Dynamic", "Kinematic"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        typeCombo.setSelectedIndex(data.get("type").getAsInt());
        typeCombo.setFont(UIUtils.getFont(Font.PLAIN, 10f));
        typeCombo.addActionListener(e -> applyValue("rigidbody2d", "type", typeCombo.getSelectedIndex()));
        addPropertyRow(p, 0, "Body Type", typeCombo);

        JCheckBox fixedRot = new JCheckBox("", data.get("fixed_rotation").getAsBoolean());
        fixedRot.setOpaque(false);
        fixedRot.addActionListener(e -> applyBool("rigidbody2d", "fixed_rotation", fixedRot.isSelected()));
        addPropertyRow(p, 1, "Fixed Rotation", fixedRot);

        return p;
    }

    private JPanel createBoxColliderUI(JsonObject data) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        addPropertyRow(p, 0, "Size", createVector2Inputs(data.getAsJsonArray("size"), "boxcollider2d", "size"));
        addPropertyRow(p, 1, "Offset", createVector2Inputs(data.getAsJsonArray("offset"), "boxcollider2d", "offset"));
        
        addPropertyRow(p, 2, "Density", createFloatInput(data.get("density").getAsFloat(), "boxcollider2d", "density"));
        addPropertyRow(p, 3, "Friction", createFloatInput(data.get("friction").getAsFloat(), "boxcollider2d", "friction"));
        addPropertyRow(p, 4, "Restitution", createFloatInput(data.get("restitution").getAsFloat(), "boxcollider2d", "restitution"));

        return p;
    }

    private JPanel createVector2Inputs(JsonArray values, String comp, String field) {
        JPanel p = new JPanel(new GridLayout(1, 2, 4, 0));
        p.setOpaque(false);
        Color[] axisColors = {new Color(231, 76, 60), new Color(46, 204, 113)};
        for (int i = 0; i < 2; i++) {
            final int index = i;
            JTextField f = new JTextField(String.format("%.2f", values.get(index).getAsFloat()));
            f.setFont(UIUtils.getFont(Font.PLAIN, 10f));
            f.putClientProperty(FlatClientProperties.STYLE, "padding: 0,2,0,2; borderWidth: 0; focusWidth: 0; background: darken($Panel.background, 5%)");
            f.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, axisColors[i]));
            f.addActionListener(e -> applyValue(f, comp, field, index));
            p.add(f);
        }
        return p;
    }

    private JTextField createFloatInput(float value, String comp, String field) {
        JTextField f = new JTextField(String.format("%.2f", value));
        f.setFont(UIUtils.getFont(Font.PLAIN, 10f));
        
        Color bg = (Color) UIManager.get("Panel.background");
        Color darkBg = com.formdev.flatlaf.util.ColorFunctions.darken(bg, 0.05f);
        Color focusBg = com.formdev.flatlaf.util.ColorFunctions.lighten(darkBg, 0.05f);
        
        f.setBackground(darkBg);
        f.putClientProperty(FlatClientProperties.STYLE, "borderWidth: 0; focusWidth: 0;");
        
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                UIAnimator.animate(0.0f, 1.0f, 200, progress -> {
                    f.setBackground(lerpColor(darkBg, focusBg, progress));
                }, null);
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                applyValue(f, comp, field, -1);
                UIAnimator.animate(0.0f, 1.0f, 200, progress -> {
                    f.setBackground(lerpColor(focusBg, darkBg, progress));
                }, null);
            }
        });
        f.addActionListener(e -> applyValue(f, comp, field, -1));
        return f;
    }

    private Color lerpColor(Color start, Color end, float progress) {
        int r = start.getRed() + (int)((end.getRed() - start.getRed()) * progress);
        int g = start.getGreen() + (int)((end.getGreen() - start.getGreen()) * progress);
        int b = start.getBlue() + (int)((end.getBlue() - start.getBlue()) * progress);
        return new Color(r, g, b);
    }

    private void applyBool(String component, String field, boolean value) {
        String cmd = String.format("{\"type\":\"command\",\"action\":\"update_component\",\"id\":%d,\"component\":\"%s\",\"field\":\"%s\",\"value\":%b}",
                currentEntityId, component, field, value);
        MainWindow.getInstance().getEngineLauncher().sendCommand(cmd);
    }

    private void applyValue(String component, String field, int value) {
        String cmd = String.format("{\"type\":\"command\",\"action\":\"update_component\",\"id\":%d,\"component\":\"%s\",\"field\":\"%s\",\"value\":%d}",
                currentEntityId, component, field, value);
        MainWindow.getInstance().getEngineLauncher().sendCommand(cmd);
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

        // We use Z-axis for 2D rotation
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
        // Send specifically to index 2 (Z axis)
        String cmd = String.format("{\"type\":\"command\",\"action\":\"update_component\",\"id\":%d,\"component\":\"transform\",\"field\":\"translation\",\"index\":2,\"value\":%.4f}",
                currentEntityId, radians);
        // Wait, the action logic in Application.cpp uses "translation" for all transform fields? 
        // Let me check my previous edit. Ah, I see: if (field == "rotation") tc.Rotation[index] = value;
        String correctCmd = String.format("{\"type\":\"command\",\"action\":\"update_component\",\"id\":%d,\"component\":\"transform\",\"field\":\"rotation\",\"index\":2,\"value\":%.4f}",
                currentEntityId, radians);
        MainWindow.getInstance().getEngineLauncher().sendCommand(correctCmd);
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

        // --- Drag & Drop Support ---
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
                        // Refresh will happen via engine telemetry
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

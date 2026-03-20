package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.parafield.storming.ui.windows.MainWindow;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Library;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a real-time viewport for the game engine's output.
 */
public class SceneViewPanel extends JPanel {

    public interface LibRT extends Library {
        LibRT INSTANCE = Native.load("rt", LibRT.class);
        int shm_open(String name, int oflag, int mode);
    }

    public interface LibC extends Library {
        LibC INSTANCE = Native.load("c", LibC.class);
        Pointer mmap(Pointer addr, long length, int prot, int flags, int fd, long offset);
        int close(int fd);
    }

    private BufferedImage image;
    private Pointer shmPtr;
    private String currentShmName;
    private int width = 1280;
    private int height = 720;
    private boolean isStreaming = false;
    private final Timer resizeTimer;

    private boolean showGrid = true;
    private final JPanel overlayToolbar;
    private String activeTool = "SELECT";
    private final Map<String, JToggleButton> toolButtons = new HashMap<>();

    private int lastMouseX, lastMouseY;

    public SceneViewPanel() {
        setLayout(null);
        setBackground(new Color(20, 20, 25));
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        resizeTimer = new Timer(150, e -> performResize());
        resizeTimer.setRepeats(false);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (getWidth() > 0 && getHeight() > 0) {
                    resizeTimer.restart();
                }
            }
        });
        
        overlayToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        overlayToolbar.setOpaque(true);
        overlayToolbar.setBackground(new Color(40, 40, 45, 220));
        overlayToolbar.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100, 50)));
        overlayToolbar.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        
        ButtonGroup toolGroup = new JButtonGroup();
        toolButtons.put("SELECT", createToolButton("Select (Q)", Icons.SELECT, "SELECT", toolGroup));
        toolButtons.put("MOVE", createToolButton("Move (W)", Icons.MOVE, "MOVE", toolGroup));
        toolButtons.put("ROTATE", createToolButton("Rotate (E)", Icons.ROTATE, "ROTATE", toolGroup));
        toolButtons.put("SCALE", createToolButton("Scale (R)", Icons.SCALE, "SCALE", toolGroup));
        
        for (String id : new String[]{"SELECT", "MOVE", "ROTATE", "SCALE"}) overlayToolbar.add(toolButtons.get(id));
        overlayToolbar.add(new JSeparator(SwingConstants.VERTICAL));
        overlayToolbar.add(createOverlayToggle("Grid", Icons.GRID, showGrid, b -> showGrid = b));
        
        overlayToolbar.setBounds(20, 15, 420, 32);
        add(overlayToolbar);

        setupShortcuts();
        setupMouseInteractions();

        Timer frameTimer = new Timer(16, e -> {
            if (isStreaming) { updateImage(); repaint(); }
            else if (currentShmName != null) attemptConnection();
        });
        frameTimer.start();
    }

    public void onEngineResized(int w, int h) {
        SwingUtilities.invokeLater(() -> {
            this.width = w;
            this.height = h;
            this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            this.shmPtr = null;
            if (currentShmName != null) attemptConnection();
        });
    }

    private void performResize() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0 || (w == width && h == height)) return;
        
        MainWindow.getInstance().getEngineLauncher().sendCommand(
            String.format("{\"type\":\"command\",\"action\":\"resize\",\"width\":%d,\"height\":%d}", w, h)
        );
    }

    private void setupMouseInteractions() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                lastMouseX = e.getX(); lastMouseY = e.getY();
                if (!isStreaming) return;
                if (activeTool.equals("SELECT")) {
                    float normX = (float) e.getX() / getWidth() * 2.0f - 1.0f;
                    float normY = 1.0f - (float) e.getY() / getHeight() * 2.0f;
                    MainWindow.getInstance().getEngineLauncher().sendCommand(
                        String.format("{\"type\":\"command\",\"action\":\"request_picking\",\"x\":%.4f,\"y\":%.4f}", normX, normY)
                    );
                }
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isStreaming || activeTool.equals("SELECT")) return;
                int dx = e.getX() - lastMouseX; int dy = e.getY() - lastMouseY;
                lastMouseX = e.getX(); lastMouseY = e.getY();
                float worldDX = (float) dx / getWidth() * 2.0f;
                float worldDY = -(float) dy / getHeight() * 2.0f;
                String cmd = "";
                switch (activeTool) {
                    case "MOVE": cmd = String.format("{\"type\":\"command\",\"action\":\"translate_selected\",\"dx\":%.4f,\"dy\":%.4f}", worldDX, worldDY); break;
                    case "ROTATE": cmd = String.format("{\"type\":\"command\",\"action\":\"rotate_selected\",\"da\":%.4f}", (float)dx * 0.05f); break;
                    case "SCALE": cmd = String.format("{\"type\":\"command\",\"action\":\"scale_selected\",\"ds\":%.4f}", (float)dx * 0.01f); break;
                }
                if (!cmd.isEmpty()) MainWindow.getInstance().getEngineLauncher().sendCommand(cmd);
            }
        };
        addMouseListener(ma); addMouseMotionListener(ma);
    }

    private void setupShortcuts() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW); ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke('q'), "selectTool"); im.put(KeyStroke.getKeyStroke('w'), "moveTool");
        im.put(KeyStroke.getKeyStroke('e'), "rotateTool"); im.put(KeyStroke.getKeyStroke('r'), "scaleTool");
        am.put("selectTool", new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { selectTool("SELECT"); } });
        am.put("moveTool", new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { selectTool("MOVE"); } });
        am.put("rotateTool", new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { selectTool("ROTATE"); } });
        am.put("scaleTool", new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { selectTool("SCALE"); } });
    }

    private void selectTool(String toolId) {
        activeTool = toolId;
        JToggleButton btn = toolButtons.get(toolId);
        if (btn != null) btn.setSelected(true);
    }

    public void setToolbarVisible(boolean visible) {
        overlayToolbar.setVisible(visible);
    }

    private JToggleButton createToolButton(String tip, Icon icon, String toolId, ButtonGroup group) {
        JToggleButton btn = new JToggleButton(icon); btn.setToolTipText(tip); btn.setPreferredSize(new Dimension(28, 28));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        if (toolId.equals("SELECT")) btn.setSelected(true);
        btn.addActionListener(e -> activeTool = toolId);
        group.add(btn); return btn;
    }

    private JToggleButton createOverlayToggle(String tip, Icon icon, boolean selected, java.util.function.Consumer<Boolean> onToggle) {
        JToggleButton btn = new JToggleButton(icon); btn.setSelected(selected); btn.setToolTipText(tip); btn.setPreferredSize(new Dimension(28, 28));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btn.addActionListener(e -> onToggle.accept(btn.isSelected()));
        return btn;
    }

    public void startStreaming(String shmName) { this.currentShmName = shmName; attemptConnection(); }
    public void stopStreaming() { isStreaming = false; shmPtr = null; currentShmName = null; }

    private void attemptConnection() {
        if (currentShmName == null) return;
        try {
            int fd = LibRT.INSTANCE.shm_open(currentShmName, 0, 0);
            if (fd >= 0) {
                shmPtr = LibC.INSTANCE.mmap(null, (long) width * height * 4, 1, 1, fd, 0);
                LibC.INSTANCE.close(fd);
                isStreaming = (shmPtr != null);
            }
        } catch (Exception ignored) {}
    }

    private void updateImage() {
        if (shmPtr == null) return;
        try {
            int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            ByteBuffer bb = shmPtr.getByteBuffer(0, (long) width * height * 4);
            IntBuffer ib = bb.asIntBuffer();
            ib.get(pixels);
        } catch (Exception ignored) {}
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.drawImage(image, 0, getHeight(), getWidth(), -getHeight(), null);
            g2.dispose();
        }
    }

    private static class JButtonGroup extends ButtonGroup {
        @Override public void add(AbstractButton b) { super.add(b); }
    }
}

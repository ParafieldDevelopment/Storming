package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Library;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;

/**
 * Provides a real-time viewport for the game engine's output.
 * Utilizes POSIX shared memory (via JNA) for high-performance image streaming from the engine process.
 * Includes a floating toolbar for scene overlays like grids, snapping, and collision indicators.
 */
public class SceneViewPanel extends JPanel {

    /** POSIX interface for Shared Memory operations. */
    public interface LibRT extends Library {
        LibRT INSTANCE = Native.load("rt", LibRT.class);
        /** Opens a shared memory object. */
        int shm_open(String name, int oflag, int mode);
    }

    /** POSIX interface for standard C library operations. */
    public interface LibC extends Library {
        LibC INSTANCE = Native.load("c", LibC.class);
        /** Maps files or devices into memory. */
        Pointer mmap(Pointer addr, long length, int prot, int flags, int fd, long offset);
        /** Closes a file descriptor. */
        int close(int fd);
    }

    private BufferedImage image;
    private String shmName;
    private Pointer shmPtr;
    private final int width = 1280;
    private final int height = 720;
    private boolean isStreaming = false;

    // 2D Overlays
    private boolean showGrid = true;
    private boolean showCollisions = false;
    private final JPanel overlayToolbar;

    /**
     * Constructs a SceneViewPanel, initializing the image buffer and floating toolbar.
     * Starts a refresh timer to handle frame updates at ~60fps.
     */
    public SceneViewPanel() {
        setLayout(null); // Absolute positioning for overlays
        setBackground(new Color(25, 25, 30));
        
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        // --- Floating Scene Toolbar ---
        overlayToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        overlayToolbar.setOpaque(true);
        overlayToolbar.setBackground(new Color(40, 40, 45, 180));
        overlayToolbar.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100, 50)));
        overlayToolbar.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        
        overlayToolbar.add(createOverlayToggle("Grid", Icons.GRID, showGrid, b -> showGrid = b));
        overlayToolbar.add(createOverlayToggle("Snap", Icons.MAGNET, true, b -> {}));
        overlayToolbar.add(createOverlayToggle("Collisions", Icons.WARN, showCollisions, b -> showCollisions = b));
        
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 20));
        overlayToolbar.add(sep);
        
        JComboBox<String> zoomCombo = new JComboBox<>(new String[]{"25%", "50%", "100%", "200%"});
        zoomCombo.setSelectedIndex(2);
        zoomCombo.putClientProperty(FlatClientProperties.STYLE, "background: #00000000; borderWidth: 0; focusWidth: 0");
        zoomCombo.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 11f));
        overlayToolbar.add(zoomCombo);

        overlayToolbar.setBounds(20, 15, 320, 32);
        add(overlayToolbar);

        // Refresh timer at ~60fps
        Timer timer = new Timer(16, e -> {
            if (isStreaming) {
                updateImage();
                repaint();
            }
        });
        timer.start();
    }

    /**
     * Toggles the visibility of the floating scene toolbar.
     * @param visible true to show the toolbar, false to hide it.
     */
    public void setToolbarVisible(boolean visible) {
        overlayToolbar.setVisible(visible);
    }

    /**
     * Utility method to create a stylized toggle button for scene overlays.
     * @param tip The tooltip text.
     * @param icon The icon to display.
     * @param selected Initial selection state.
     * @param onToggle Callback to execute when the toggle state changes.
     * @return A configured JToggleButton.
     */
    private JToggleButton createOverlayToggle(String tip, Icon icon, boolean selected, java.util.function.Consumer<Boolean> onToggle) {
        JToggleButton btn = new JToggleButton(icon);
        btn.setSelected(selected);
        btn.setToolTipText(tip);
        btn.setPreferredSize(new Dimension(28, 28));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btn.addActionListener(e -> onToggle.accept(btn.isSelected()));
        return btn;
    }

    /**
     * Initiates image streaming from the specified shared memory segment.
     * @param shmName The name of the shared memory object.
     */
    public void startStreaming(String shmName) {
        this.shmName = shmName;
        try {
            int fd = LibRT.INSTANCE.shm_open(shmName, 0, 0);
            if (fd < 0) return;
            shmPtr = LibC.INSTANCE.mmap(null, (long) width * height * 4, 1, 1, fd, 0);
            LibC.INSTANCE.close(fd);
            isStreaming = true;
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Stops the image streaming process.
     */
    public void stopStreaming() {
        isStreaming = false;
        shmPtr = null;
    }

    /**
     * Reads pixel data from shared memory and updates the internal image buffer.
     */
    private void updateImage() {
        if (shmPtr == null) return;
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        ByteBuffer bb = shmPtr.getByteBuffer(0, (long) width * height * 4);
        for (int i = 0; i < pixels.length; i++) {
            int r = bb.get() & 0xFF; int g = bb.get() & 0xFF; int b = bb.get() & 0xFF; int a = bb.get() & 0xFF;
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        bb.rewind();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            // Fill background
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw image centered and scaled
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            
            // Draw custom grid if enabled (only if toolbar/overlays would be visible)
            if (showGrid && overlayToolbar.isVisible()) {
                drawGrid(g);
            }
        }
    }

    /**
     * Draws a debug grid overlay on the scene viewport.
     * @param g The Graphics context to draw on.
     */
    private void drawGrid(Graphics g) {
        g.setColor(new Color(200, 200, 200, 20));
        int step = 32;
        for (int x = 0; x < getWidth(); x += step) g.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += step) g.drawLine(0, y, getWidth(), y);
    }

    /**
     * Returns a native window ID for engine integration (if applicable).
     * @return The native window handle, or 0 if not supported.
     */
    public long getNativeWindowID() { return 0; }
}

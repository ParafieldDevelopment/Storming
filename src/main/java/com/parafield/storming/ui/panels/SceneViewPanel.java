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
import java.nio.IntBuffer;

/**
 * Provides a real-time viewport for the game engine's output.
 * Utilizes POSIX shared memory (via JNA) for high-performance image streaming from the engine process.
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
    private final int width = 1280;
    private final int height = 720;
    private boolean isStreaming = false;

    private boolean showGrid = true;
    private boolean showCollisions = false;
    private final JPanel overlayToolbar;

    public SceneViewPanel() {
        setLayout(null);
        setBackground(new Color(25, 25, 30));
        
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
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

        Timer timer = new Timer(16, e -> {
            if (isStreaming) {
                updateImage();
                repaint();
            }
        });
        timer.start();
    }

    public void setToolbarVisible(boolean visible) {
        overlayToolbar.setVisible(visible);
    }

    private JToggleButton createOverlayToggle(String tip, Icon icon, boolean selected, java.util.function.Consumer<Boolean> onToggle) {
        JToggleButton btn = new JToggleButton(icon);
        btn.setSelected(selected);
        btn.setToolTipText(tip);
        btn.setPreferredSize(new Dimension(28, 28));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btn.addActionListener(e -> onToggle.accept(btn.isSelected()));
        return btn;
    }

    public void startStreaming(String shmName) {
        try {
            int fd = LibRT.INSTANCE.shm_open(shmName, 0, 0); // O_RDONLY = 0
            if (fd < 0) {
                System.err.println("[Java] Failed to open SHM: " + shmName);
                return;
            }
            // prot=1 (READ), flags=1 (SHARED)
            shmPtr = LibC.INSTANCE.mmap(null, (long) width * height * 4, 1, 1, fd, 0);
            LibC.INSTANCE.close(fd);
            isStreaming = true;
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void stopStreaming() {
        isStreaming = false;
        shmPtr = null;
    }

    private void updateImage() {
        if (shmPtr == null) return;
        
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        ByteBuffer bb = shmPtr.getByteBuffer(0, (long) width * height * 4);
        IntBuffer ib = bb.asIntBuffer();
        
        // Fast Bulk Read
        ib.get(pixels);
        
        // Fix ABGR to ARGB if necessary. OpenGL GL_RGBA results in 0xAABBGGRR in Little Endian.
        // TYPE_INT_ARGB expects 0xAARRGGBB.
        for (int i = 0; i < pixels.length; i++) {
            int abgr = pixels[i];
            int r = (abgr & 0xFF);
            int g = (abgr >> 8) & 0xFF;
            int b = (abgr >> 16) & 0xFF;
            int a = (abgr >> 24) & 0xFF;
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            // GL Origin is Bottom-Left. Java is Top-Left. 
            // Scaling by -1 vertically and offsetting by height flips it.
            g2.drawImage(image, 0, getHeight(), getWidth(), -getHeight(), null);
            
            if (showGrid && overlayToolbar.isVisible()) {
                drawGrid(g2);
            }
            g2.dispose();
        }
    }

    private void drawGrid(Graphics g) {
        g.setColor(new Color(200, 200, 200, 20));
        int step = 32;
        for (int x = 0; x < getWidth(); x += step) g.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += step) g.drawLine(0, y, getWidth(), y);
    }
}

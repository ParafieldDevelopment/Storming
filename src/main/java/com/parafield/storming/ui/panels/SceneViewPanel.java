package com.parafield.storming.ui.panels;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Library;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;

public class SceneViewPanel extends JPanel {

    // Define POSIX interface for Shared Memory
    public interface LibRT extends Library {
        LibRT INSTANCE = Native.load("rt", LibRT.class);
        int shm_open(String name, int oflag, int mode);
    }

    public interface LibC extends Library {
        LibC INSTANCE = Native.load("c", LibC.class);
        Pointer mmap(Pointer addr, long length, int prot, int flags, int fd, long offset);
        int munmap(Pointer addr, long length);
        int close(int fd);
        int shm_unlink(String name); // Add shm_unlink
    }

    private BufferedImage image;
    private String shmName;
    private Pointer shmPtr;
    private Object shmHandle; // Handle for Windows, can be a WinNT.HANDLE
    private final int width = 1280;
    private final int height = 720;
    private boolean isStreaming = false;

    private final String osName = System.getProperty("os.name").toLowerCase();
    private final boolean isWindows = osName.contains("win");

    public SceneViewPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 46));
        
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        // Refresh timer at ~60fps
        Timer timer = new Timer(16, e -> {
            if (isStreaming) {
                updateImage();
                repaint();
            }
        });
        timer.start();
    }

    public void startStreaming(String shmName) {
        this.shmName = shmName;
        try {
            if (isWindows) {
                shmHandle = Kernel32.INSTANCE.OpenFileMapping(WinNT.FILE_MAP_ALL_ACCESS, false, shmName);
                if (shmHandle == null) {
                    System.err.println("[System] Failed to open Shared Memory (Windows): " + shmName + " Error: " + Kernel32.INSTANCE.GetLastError());
                    return;
                }
                shmPtr = Kernel32.INSTANCE.MapViewOfFile((WinNT.HANDLE)shmHandle, WinNT.FILE_MAP_ALL_ACCESS, 0, 0, (int) ((long) width * height * 4));
                if (shmPtr == null) {
                    System.err.println("[System] Failed to map Shared Memory (Windows): " + shmName + " Error: " + Kernel32.INSTANCE.GetLastError());
                    Kernel32.INSTANCE.CloseHandle((WinNT.HANDLE)shmHandle);
                    shmHandle = null;
                    return;
                }
            } else { // POSIX
                // 0 = O_RDONLY
                int fd = LibRT.INSTANCE.shm_open(shmName, 0, 0);
                if (fd < 0) {
                    System.err.println("[System] Failed to open Shared Memory (POSIX): " + shmName + " Error: " + Native.getLastError());
                    return;
                }

                // PROT_READ = 1, MAP_SHARED = 1
                shmPtr = LibC.INSTANCE.mmap(null, (long) width * height * 4, 1, 1, fd, 0);
                LibC.INSTANCE.close(fd); // Close the file descriptor, mapping remains active
                if (shmPtr == null) {
                    System.err.println("[System] Failed to map Shared Memory (POSIX): " + shmName + " Error: " + Native.getLastError());
                    return;
                }
            }
            
            isStreaming = true;
            System.out.println("[System] Started streaming from: " + shmName);
        } catch (Exception e) {
            System.err.println("[System] Error in startStreaming: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopStreaming() {
        if (!isStreaming) return;

        isStreaming = false;
        if (shmPtr != null) {
            if (isWindows) {
                Kernel32.INSTANCE.UnmapViewOfFile(shmPtr);
                if (shmHandle != null) {
                    Kernel32.INSTANCE.CloseHandle((WinNT.HANDLE)shmHandle);
                }
            } else { // POSIX
                LibC.INSTANCE.munmap(shmPtr, (long) width * height * 4);
                LibC.INSTANCE.shm_unlink(shmName); // Unlink for cleanup
            }
            shmPtr = null;
            shmHandle = null;
            System.out.println("[System] Stopped streaming from: " + shmName);
        }
    }

    private void updateImage() {
        if (shmPtr == null) return;
        
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        ByteBuffer bb = shmPtr.getByteBuffer(0, (long) width * height * 4);
        
        // Simple pixel copy and channel swap
        for (int i = 0; i < pixels.length; i++) {
            int r = bb.get() & 0xFF;
            int g = bb.get() & 0xFF;
            int b = bb.get() & 0xFF;
            int a = bb.get() & 0xFF;
            // OpenGL (RGBA) -> Java (ARGB)
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        bb.rewind();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            // Draw image centered and scaled
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
    }

    public long getNativeWindowID() {
        return 0; // Legacy
    }

    public String getShmName() {
        return shmName;
    }
}

package com.parafield.storming;

import javax.swing.ImageIcon;
import java.awt.*;
import java.util.Objects;

public class Icons {
    public static final Image FRAME_ICON = Toolkit.getDefaultToolkit().getImage(Icons.class.getResource("/com/parafield/storming/icons/icon.png"));

    private static ImageIcon loadIcon(String name, int width, int height) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(Icons.class.getResource("/com/parafield/storming/icons/" + name + ".png")));
        if (width > 0 && height > 0) {
            icon = new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }
        return icon;
    }

    public static final ImageIcon FOLDER = loadIcon("folder", 20, 20);
    public static final ImageIcon SEARCH = loadIcon("search", 20, 20);
    public static final ImageIcon HAMBURGER = loadIcon("hamburger", 23, 23);
    public static final ImageIcon PLAY = loadIcon("play", 23, 23);
    public static final ImageIcon STOP = loadIcon("stop", 23, 23);
    public static final ImageIcon SETTINGS = loadIcon("settings", 20, 20);
    public static final ImageIcon BELL = loadIcon("bell", 20, 20);
    public static final ImageIcon CONSOLE = loadIcon("console", 20, 20);
    public static final ImageIcon WARN = loadIcon("warn", 20, 20);
    public static final ImageIcon LOGO = loadIcon("icon", 128, 128);
}
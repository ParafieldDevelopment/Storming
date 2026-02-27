package com.parafield.storming;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.util.Objects;

public class Icons {
    private static FlatSVGIcon loadSVG(String name, int size) {
        return new FlatSVGIcon("com/parafield/storming/icons/" + name + ".svg", size, size);
    }
    
    private static ImageIcon loadPNG(String name, int width, int height) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(Icons.class.getResource("/com/parafield/storming/icons/" + name + ".png")));
        if (width > 0 && height > 0) {
            icon = new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }
        return icon;
    }

    public static final FlatSVGIcon FOLDER = loadSVG("folder", 22);
    public static final FlatSVGIcon SEARCH = loadSVG("search", 22);
    public static final FlatSVGIcon HAMBURGER = loadSVG("hamburger", 22);
    public static final FlatSVGIcon PLAY = loadSVG("play", 20);
    public static final FlatSVGIcon STOP = loadSVG("stop", 20);
    public static final FlatSVGIcon SETTINGS = loadSVG("settings", 22);
    public static final FlatSVGIcon BELL = loadSVG("bell", 22);
    public static final FlatSVGIcon CONSOLE = loadSVG("console", 18);
    public static final FlatSVGIcon WARN = loadSVG("warn", 18);

    // Frame/taskbar icon
    public static Image frameIcon() {
        return loadPNG("icon", 0, 0).getImage();
    }
}

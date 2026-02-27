package com.parafield.storming;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.util.Objects;

public class Icons {
    private static ImageIcon loadIcon(String name, int width, int height) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(Icons.class.getResource("/com/parafield/storming/icons/" + name + ".png")));
        if (width > 0 && height > 0) {
            icon = new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }
        return icon;
    }

    public static final ImageIcon FOLDER = loadIcon("folder", 20, 20);
    public static final ImageIcon SEARCH = loadIcon("search", 20, 20);
    public static final ImageIcon HAMBURGER = loadIcon("hamburger", 18, 18);
    public static final ImageIcon PLAY = loadIcon("play", 16, 16);
    public static final ImageIcon STOP = loadIcon("stop", 16, 16);
    public static final ImageIcon SETTINGS = loadIcon("settings", 20, 20);

    // Frame/taskbar icon
    public static Image frameIcon() {
        return loadIcon("icon", 0, 0).getImage();
    }
}
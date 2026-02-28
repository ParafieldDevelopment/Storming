package com.parafield.storming;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.ImageIcon;
import java.awt.*;
import java.net.URL;
import java.util.Objects;

public class Icons {
    public static final Image FRAME_ICON = Toolkit.getDefaultToolkit().getImage(Icons.class.getResource("/com/parafield/storming/icons/exe-icon.png"));

    private static ImageIcon loadIcon(String name, int width, int height) {
        String svgPath = "/com/parafield/storming/icons/" + name + ".svg";
        URL svgUrl = Icons.class.getResource(svgPath);
        
        if (svgUrl != null) {
            FlatSVGIcon svgIcon = new FlatSVGIcon(svgUrl);
            if (width > 0 && height > 0) {
                return svgIcon.derive(width, height);
            }
            return svgIcon;
        } else {
            // Fallback to PNG
            URL pngUrl = Icons.class.getResource("/com/parafield/storming/icons/" + name + ".png");
            if (pngUrl == null) {
                return null;
            }
            ImageIcon icon = new ImageIcon(pngUrl);
            if (width > 0 && height > 0) {
                icon = new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }
            return icon;
        }
    }

    public static final ImageIcon FOLDER = loadIcon("folder", 20, 20);
    public static final ImageIcon FOLDER_80 = loadIcon("folder", 80, 80);
    
    public static final ImageIcon SEARCH = loadIcon("search", 20, 20);
    public static final ImageIcon SEARCH_80 = loadIcon("search", 80, 80);
    
    public static final ImageIcon HAMBURGER = loadIcon("hamburger", 20, 20);
    public static final ImageIcon PLAY = loadIcon("play", 23, 23);
    public static final ImageIcon STOP = loadIcon("stop", 23, 23);
    
    public static final ImageIcon SETTINGS = loadIcon("settings", 20, 20);
    public static final ImageIcon SETTINGS_80 = loadIcon("settings", 80, 80);
    
    public static final ImageIcon BELL = loadIcon("bell", 20, 20);
    public static final ImageIcon BELL_80 = loadIcon("bell", 80, 80);
    
    public static final ImageIcon CONSOLE = loadIcon("console", 20, 20);
    public static final ImageIcon CONSOLE_80 = loadIcon("console", 80, 80);
    
    public static final ImageIcon TERMINAL = loadIcon("console", 20, 20);
    public static final ImageIcon TERMINAL_80 = loadIcon("console", 80, 80);
    
    public static final ImageIcon WARN = loadIcon("warn", 20, 20);
    public static final ImageIcon WARN_80 = loadIcon("warn", 80, 80);
    
    public static final ImageIcon LOGO = loadIcon("icon", 128, 128);
    public static final ImageIcon LOGO_80 = loadIcon("icon", 80, 80);
    
    public static final ImageIcon EXE_ICON = loadIcon("exe-icon", 18, 18);
    public static final ImageIcon GIT = loadIcon("Git", 18, 18);
    public static final ImageIcon GRID = loadIcon("grid", 16, 16);
    public static final ImageIcon MAGNET = loadIcon("magnet", 16, 16);
    public static final ImageIcon PLUS = loadIcon("plus", 16, 16);
    public static final ImageIcon BRUSH = loadIcon("brush", 16, 16);
    public static final ImageIcon CLIPBOARD = loadIcon("clipboard", 16, 16);
}

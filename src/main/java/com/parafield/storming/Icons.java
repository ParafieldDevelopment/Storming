package com.parafield.storming;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.ImageIcon;
import java.awt.*;
import java.net.URL;
import java.util.Objects;

/**
 * Provides access to SVG and PNG icon resources used throughout the Storming Engine Editor.
 * Automatically handles SVG loading (using FlatLaf) and PNG fallbacks.
 * SVGs are automatically tinted to match the current theme (light/dark).
 */
public class Icons {
    /** The main window icon for the application. */
    public static final Image FRAME_ICON = Toolkit.getDefaultToolkit().getImage(Icons.class.getResource("/com/parafield/storming/icons/png/exe-icon.png"));

    /**
     * Loads an icon from resources by name.
     * Attempts to find a .svg file in icons/svg first, falling back to .png in icons/png if not found.
     * SVGs are configured with a theme-aware color filter.
     *
     * @param name The base name of the icon file.
     * @param width The target width to scale to (set to 0 for default).
     * @param height The target height to scale to (set to 0 for default).
     * @return An ImageIcon if found, null otherwise.
     */
    private static ImageIcon loadIcon(String name, int width, int height) {
        String svgPath = "/com/parafield/storming/icons/svg/" + name + ".svg";
        URL svgUrl = Icons.class.getResource(svgPath);
        
        if (svgUrl != null) {
            FlatSVGIcon svgIcon = new FlatSVGIcon(svgUrl);
            
            // Apply dynamic theme-aware color filter for all icons except brand icons
            if (!name.equalsIgnoreCase("icon") && !name.equalsIgnoreCase("exe-icon")) {
                svgIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> {
                    // If the current theme is light, map white icons to dark gray
                    if (!FlatLaf.isLafDark()) {
                        return new Color(60, 60, 60);
                    }
                    // Otherwise keep original (white)
                    return color;
                }));
            }

            if (width > 0 && height > 0) {
                return svgIcon.derive(width, height);
            }
            return svgIcon;
        } else {
            // Fallback to PNG
            URL pngUrl = Icons.class.getResource("/com/parafield/storming/icons/png/" + name + ".png");
            if (pngUrl == null) {
                return null;
            }
            
            ImageIcon icon = new ImageIcon(pngUrl);
            
            // Note: PNG inversion/tinting is more complex and usually handled via separate assets 
            // or specific ImageFilters if needed. For now, we prioritize SVG scaling and tinting.
            
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
    
    public static final ImageIcon HAMBURGER = loadIcon("hamburger", 30, 30);
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
    public static final ImageIcon COMMIT = loadIcon("commit", 18, 18);
    public static final ImageIcon PR = loadIcon("PR", 18, 18);
    public static final ImageIcon GRID = loadIcon("grid", 16, 16);
    public static final ImageIcon MAGNET = loadIcon("magnet", 16, 16);
    public static final ImageIcon PLUS = loadIcon("plus", 16, 16);
    public static final ImageIcon BRUSH = loadIcon("brush", 16, 16);
    public static final ImageIcon CLIPBOARD = loadIcon("clipboard", 16, 16);
    public static final ImageIcon RESTART = loadIcon("restart", 23, 23);
    public static final ImageIcon PAUSE = loadIcon("pause", 23, 23);
    public static final ImageIcon PIN = loadIcon("pin", 18, 18);
    public static final ImageIcon CAMERA = loadIcon("camera", 18, 18);
    public static final ImageIcon FULLSCREEN = loadIcon("fullscreen", 18, 18);
    public static final ImageIcon STRUCTURE = loadIcon("Structure", 18, 18);
    public static final ImageIcon PERFORMANCE = loadIcon("performance", 18, 18);
}

package com.parafield.storming.ui.utils;

import javax.swing.UIManager;
import java.awt.Font;

/**
 * Static utility methods for UI-related tasks, such as safe font retrieval and component styling.
 */
public class UIUtils {
    /**
     * Safely retrieves a UI font from the UIManager, falling back to standard fonts if necessary.
     * @param style The font style (e.g., Font.BOLD, Font.PLAIN).
     * @param size The target font size.
     * @return A Font instance with the requested style and size.
     */
    public static Font getFont(int style, float size) {
        Font f = UIManager.getFont("defaultFont");
        if (f == null) f = UIManager.getFont("Label.font");
        if (f == null) f = new Font("SansSerif", style, (int)size);
        return f.deriveFont(style, size);
    }
}

package com.parafield.storming.ui.utils;

import javax.swing.UIManager;
import java.awt.Font;

public class UIUtils {
    /**
     * Safely gets a UI font, falling back to Label.font if needed.
     */
    public static Font getFont(int style, float size) {
        Font f = UIManager.getFont("defaultFont");
        if (f == null) f = UIManager.getFont("Label.font");
        if (f == null) f = new Font("SansSerif", style, (int)size);
        return f.deriveFont(style, size);
    }
}

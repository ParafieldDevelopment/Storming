package com.parafield.storming;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.parafield.storming.core.DiscordRPCManager;
import com.parafield.storming.ui.windows.ProjectSelectorWindow;
import com.parafield.storming.ui.windows.SplashWindow;
import javax.swing.*;
import java.awt.*;

/**
 * Main entry point for the Storming Engine Editor application.
 * Handles the initial setup, global theme application, and application lifecycle.
 */
public class EditorApp {

    /**
     * Entry point of the application.
     * Configures global UI properties, applies the theme, and launches the initial windows.
     *
     * @param args Command-line arguments (not currently used).
     */
    public static void main(String[] args) {
        // Unified window decorations for all platforms
        System.setProperty("flatlaf.useWindowDecorations", "true");
        System.setProperty("flatlaf.menuBarEmbedded", "true");
        
        // Hide icons and remove gaps in title pane
        UIManager.put("TitlePane.showIcon", false);
        UIManager.put("TitlePane.embeddedMenuBarGap", 0);

        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // Initialize Discord Rich Presence
        DiscordRPCManager.init();

        // Shutdown hook to close Discord RPC cleanly
        Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPCManager::shutdown));

        SwingUtilities.invokeLater(() -> {
            // Apply theme globally
            setupGlobalTheme();

            // 1. Show Splash Screen
            SplashWindow splash = new SplashWindow("/com/parafield/storming/splashscreen.png");
            splash.showSplash();

            // 2. Show WIP Dialog
            JOptionPane.showMessageDialog(null, 
                "Storming Engine Rework is currently in early development (WIP).\nMany features are not yet implemented.", 
                "Storming Development Branch", 
                JOptionPane.INFORMATION_MESSAGE);

            // 3. Launch Project Selector (The initial state)
            ProjectSelectorWindow projectSelector = new ProjectSelectorWindow();
            projectSelector.setVisible(true);

            // 4. Close Splash
            splash.close();
        });
    }

    /**
     * Configures the global Look and Feel and UI refinements for a consistent, modern appearance.
     * Uses FlatLaf for a Mac-style dark theme and customizes component arcs and tab heights.
     */
    private static void setupGlobalTheme() {
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
            
            // Refine UI for "Cleanliness"
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.tabHeight", 32);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ScrollBar.thumbArc", 999);
            
        } catch (Exception ex) {
            System.err.println("Theme Error: " + ex.getMessage());
        }
    }
}

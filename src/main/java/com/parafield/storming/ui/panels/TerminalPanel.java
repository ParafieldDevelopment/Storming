package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.parafield.storming.Icons;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TerminalPanel extends JPanel {

    private final JTabbedPane tabbedPane;
    private int sessionCounter = 1;

    public TerminalPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 20));

        tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if (index != -1) showTabContextMenu(e.getComponent(), e.getX(), e.getY(), index);
                }
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        toolbar.setBackground(new Color(25, 25, 30));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(60, 60, 60)));
        
        JButton newTabBtn = createToolbarBtn("New Session", Icons.PLUS);
        newTabBtn.addActionListener(e -> addNewSession());
        
        JButton clearBtn = createToolbarBtn("Clear", Icons.BRUSH);
        clearBtn.addActionListener(e -> {
            TerminalSession s = getCurrentSession();
            if (s != null) s.clear();
        });

        toolbar.add(Box.createVerticalStrut(5));
        toolbar.add(newTabBtn);
        toolbar.addSeparator();
        toolbar.add(clearBtn);
        toolbar.add(Box.createVerticalGlue());
        add(toolbar, BorderLayout.EAST);

        addNewSession();
    }

    private JButton createToolbarBtn(String tip, Icon icon) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tip);
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        btn.setMaximumSize(new Dimension(32, 32));
        return btn;
    }

    private void addNewSession() {
        TerminalSession session = new TerminalSession();
        tabbedPane.addTab("Session " + (sessionCounter++), session);
        tabbedPane.setSelectedComponent(session);
    }

    private TerminalSession getCurrentSession() {
        Component selected = tabbedPane.getSelectedComponent();
        return (selected instanceof TerminalSession) ? (TerminalSession) selected : null;
    }

    private void showTabContextMenu(Component invoker, int x, int y, int index) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem renameItem = new JMenuItem("Rename Tab...");
        renameItem.addActionListener(e -> {
            String newName = JOptionPane.showInputDialog(this, "Enter tab name:", tabbedPane.getTitleAt(index));
            if (newName != null && !newName.trim().isEmpty()) tabbedPane.setTitleAt(index, newName);
        });
        JMenuItem closeItem = new JMenuItem("Close Session");
        closeItem.addActionListener(e -> {
            TerminalSession session = (TerminalSession) tabbedPane.getComponentAt(index);
            session.stop();
            tabbedPane.removeTabAt(index);
        });
        menu.add(renameItem); menu.addSeparator(); menu.add(closeItem);
        menu.show(invoker, x, y);
    }

    private static class StormingTerminalSettings extends DefaultSettingsProvider {
        @Override
        public ColorPalette getTerminalColorPalette() {
            return new ColorPalette() {
                @Override
                protected com.jediterm.core.Color getForegroundByColorIndex(int index) {
                    return getByColorIndex(index);
                }

                @Override
                protected com.jediterm.core.Color getBackgroundByColorIndex(int index) {
                    return getByColorIndex(index);
                }

                private com.jediterm.core.Color getByColorIndex(int index) {
                    switch (index) {
                        case 0: return new com.jediterm.core.Color(40, 44, 52); // Black
                        case 1: return new com.jediterm.core.Color(231, 76, 60); // Red
                        case 2: return new com.jediterm.core.Color(46, 204, 113); // Green
                        case 3: return new com.jediterm.core.Color(241, 196, 15); // Yellow
                        case 4: return new com.jediterm.core.Color(52, 152, 219); // Blue
                        case 5: return new com.jediterm.core.Color(155, 89, 182); // Magenta
                        case 6: return new com.jediterm.core.Color(26, 188, 156); // Cyan
                        case 7: return new com.jediterm.core.Color(220, 220, 220); // White
                        default: return null;
                    }
                }
            };
        }

        @Override
        public TextStyle getDefaultStyle() {
            return new TextStyle(
                    new TerminalColor(220, 220, 220), // Foreground
                    new TerminalColor(15, 15, 20)     // Background
            );
        }

        @Override
        public TextStyle getSelectionColor() {
            return new TextStyle(
                    new TerminalColor(220, 220, 220),
                    new TerminalColor(60, 60, 70)
            );
        }

        @Override
        public Font getTerminalFont() {
            String os = System.getProperty("os.name").toLowerCase();
            String fontName = "JetBrains Mono";
            int fontSize = os.contains("linux") ? 14 : 13; // 14 usually looks better on Linux/X11
            
            if (os.contains("linux")) {
                String[] linuxFonts = {"JetBrains Mono", "DejaVu Sans Mono", "Fira Code", "Liberation Mono", "Monospaced"};
                for (String f : linuxFonts) {
                    Font font = new Font(f, Font.PLAIN, fontSize);
                    if (!font.getFamily().equals("Dialog") || f.equals("Monospaced")) {
                        fontName = f;
                        break;
                    }
                }
            }
            
            return new Font(fontName, Font.PLAIN, fontSize);
        }

        @Override
        public float getTerminalFontSize() {
            return System.getProperty("os.name").toLowerCase().contains("linux") ? 14.0f : 13.0f;
        }

        @Override
        public float getLineSpacing() {
            return 1.2f; // Increased slightly for better readability
        }

        @Override
        public boolean useAntialiasing() {
            return true;
        }

        @Override
        public int caretBlinkingMs() {
            return 500;
        }

        @Override
        public int getBufferMaxLinesCount() {
            return 5000;
        }
    }

    private static class TerminalSession extends JPanel {
        private JediTermWidget terminalWidget;
        private PtyProcess process;

        public TerminalSession() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setBackground(new Color(15, 15, 20));
            
            terminalWidget = new JediTermWidget(new StormingTerminalSettings());
            terminalWidget.setTtyConnector(createTtyConnector());
            terminalWidget.start();

            add(terminalWidget, BorderLayout.CENTER);
        }

        private TtyConnector createTtyConnector() {
            try {
                Map<String, String> env = new HashMap<>(System.getenv());
                env.put("TERM", "xterm-256color");
                
                String os = System.getProperty("os.name").toLowerCase();
                String shell = env.get("SHELL");
                if (shell == null) shell = os.contains("win") ? "powershell.exe" : "/bin/bash";

                String[] command = os.contains("win") ? new String[]{shell, "-NoLogo"} : new String[]{shell, "-i"};
                
                process = new PtyProcessBuilder(command)
                        .setEnvironment(env)
                        .setDirectory(System.getProperty("user.dir"))
                        .start();

                return new com.jediterm.terminal.ProcessTtyConnector(process, StandardCharsets.UTF_8) {
                    @Override
                    public String getName() {
                        return "Local Terminal";
                    }
                };
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void clear() {
            terminalWidget.getTerminal().clearScreen();
        }

        public void stop() {
            terminalWidget.stop();
            if (process != null) process.destroy();
        }
    }
}

package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.jediterm.terminal.TtyConnector;
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

    private static class TerminalSession extends JPanel {
        private JediTermWidget terminalWidget;
        private PtyProcess process;

        public TerminalSession() {
            setLayout(new BorderLayout());
            
            DefaultSettingsProvider settings = new DefaultSettingsProvider() {
                /*
                @Override
                public Color paletteRed() { return new Color(231, 76, 60); }
                @Override
                public Color paletteGreen() { return new Color(46, 204, 113); }
                @Override
                public Color paletteYellow() { return new Color(241, 196, 15); }
                @Override
                public Color paletteBlue() { return new Color(52, 152, 219); }
                */
                
                @Override
                public Font getTerminalFont() {
                    return new Font("JetBrains Mono", Font.PLAIN, 13);
                }

                @Override
                public float getTerminalFontSize() {
                    return 13.0f;
                }
            };

            terminalWidget = new JediTermWidget(settings);
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

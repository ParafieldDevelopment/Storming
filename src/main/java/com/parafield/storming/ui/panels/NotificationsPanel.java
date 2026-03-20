package com.parafield.storming.ui.panels;

import com.parafield.storming.Icons;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Displays user notifications, system alerts, and engine messages.
 */
public class NotificationsPanel extends JPanel {
    
    private final JPanel listPanel;
    private final JScrollPane scrollPane;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public NotificationsPanel() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        
        showEmptyState();
        add(scrollPane, BorderLayout.CENTER);
    }

    private void showEmptyState() {
        listPanel.removeAll();
        listPanel.add(Box.createVerticalGlue());
        JLabel label = new JLabel("No new notifications.", SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(UIManager.getColor("Label.disabledForeground"));
        listPanel.add(label);
        listPanel.add(Box.createVerticalGlue());
        listPanel.revalidate();
    }

    public void addNotification(String type, String title, String message) {
        SwingUtilities.invokeLater(() -> {
            if (listPanel.getComponentCount() > 0 && listPanel.getComponent(1) instanceof JLabel) {
                listPanel.removeAll();
            }

            JPanel item = new JPanel(new BorderLayout(10, 5));
            item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            item.setBackground(new Color(40, 40, 45));
            item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 15)),
                new EmptyBorder(10, 15, 10, 15)
            ));

            JLabel iconLabel = new JLabel(type.equalsIgnoreCase("CRITICAL") ? Icons.WARN : Icons.BELL);
            item.add(iconLabel, BorderLayout.WEST);

            JPanel content = new JPanel(new GridLayout(2, 1));
            content.setOpaque(false);
            
            JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
            titleLabel.setForeground(type.equalsIgnoreCase("CRITICAL") ? new Color(231, 76, 60) : Color.WHITE);
            content.add(titleLabel);

            JLabel msgLabel = new JLabel(message);
            msgLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            msgLabel.setForeground(new Color(180, 180, 185));
            content.add(msgLabel);

            item.add(content, BorderLayout.CENTER);

            JLabel timeLabel = new JLabel(timeFormat.format(new Date()));
            timeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            timeLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
            item.add(timeLabel, BorderLayout.EAST);

            listPanel.add(item, 0); // Add at top
            listPanel.revalidate();
            listPanel.repaint();
        });
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (listPanel.getComponentCount() == 0 || (listPanel.getComponentCount() == 3 && listPanel.getComponent(1) instanceof JLabel)) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
            int size = 120;
            int x = getWidth() - size - 40;
            int y = getHeight() - size - 40;
            if (Icons.BELL_80 != null) Icons.BELL_80.paintIcon(this, g2, x, y);
            g2.dispose();
        }
    }
}

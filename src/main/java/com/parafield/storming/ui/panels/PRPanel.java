package com.parafield.storming.ui.panels;

import com.formdev.flatlaf.FlatClientProperties;
import com.parafield.storming.Icons;
import com.parafield.storming.ui.windows.MainWindow;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Displays a list of Pull Requests associated with the current project.
 * [WIP] This component is currently a visual placeholder and uses mock data.
 * Future versions will implement actual integration with version control hosting providers.
 */
public class PRPanel extends JPanel {

    private final JPanel prList;
    private final CardLayout cardLayout;
    private final JPanel mainContainer;

    /**
     * Constructs a PRPanel, initializing the PR list with mock data.
     */
    public PRPanel() {
        setLayout(new BorderLayout());
        
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        // --- 1. PR List View ---
        JPanel listView = new JPanel(new BorderLayout());
        
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
        header.setPreferredSize(new Dimension(0, 32));
        header.setBackground(UIManager.getColor("Panel.background"));

        JLabel title = new JLabel("  Pull Requests", Icons.PR, SwingConstants.LEFT);
        title.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 11f));
        header.add(title, BorderLayout.WEST);

        JButton createBtn = new JButton(Icons.PLUS);
        createBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        createBtn.setToolTipText("New Pull Request");
        header.add(createBtn, BorderLayout.EAST);

        listView.add(header, BorderLayout.NORTH);

        prList = new JPanel();
        prList.setLayout(new BoxLayout(prList, BoxLayout.Y_AXIS));
        prList.setBackground(UIManager.getColor("TextArea.background"));
        
        addPRItem("#42: Refactor Renderer API", "batista", "2 hours ago");
        addPRItem("#41: Add Vulkan backend support", "parafield", "Yesterday");
        addPRItem("#40: Fix crash on window resize", "contributor1", "3 days ago");

        JScrollPane scrollPane = new JScrollPane(prList);
        scrollPane.setBorder(null);
        listView.add(scrollPane, BorderLayout.CENTER);

        JButton createPRBigBtn = new JButton("Create Pull Request...");
        createPRBigBtn.putClientProperty(FlatClientProperties.STYLE, "background: #27ae60; foreground: #ffffff;");
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(new EmptyBorder(10, 10, 10, 10));
        footer.add(createPRBigBtn, BorderLayout.CENTER);
        listView.add(footer, BorderLayout.SOUTH);

        mainContainer.add(listView, "LIST");
        add(mainContainer, BorderLayout.CENTER);
    }

    /**
     * Adds a Pull Request item to the list.
     * @param title The PR title.
     * @param author The PR author.
     * @param time When the PR was created/updated.
     */
    private void addPRItem(String title, String author, String time) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        item.setPreferredSize(new Dimension(0, 50));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDetails(title, author);
                }
            }
        });

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 12f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel infoLabel = new JLabel(author + " • " + time);
        infoLabel.setFont(UIManager.getFont("defaultFont").deriveFont(10f));
        infoLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        item.add(titleLabel);
        item.add(Box.createVerticalStrut(2));
        item.add(infoLabel);
        
        prList.add(item);
    }

    /**
     * Switches the view to show details for a specific Pull Request.
     * @param title The PR title.
     * @param author The PR author.
     */
    public void showDetails(String title, String author) {
        PRDetailsSidePanel detailView = new PRDetailsSidePanel(title, () -> cardLayout.show(mainContainer, "LIST"));
        mainContainer.add(detailView, "DETAIL");
        cardLayout.show(mainContainer, "DETAIL");
        
        MainWindow.getInstance().openPRDetails(title, author);
    }
}

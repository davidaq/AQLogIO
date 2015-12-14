package com.davidaq.logio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

public class TabPage {

    public TabPage(Content content) {
        this.content = content;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (titleLabel != null) {
                    titleLabel.setText(content.getTitle());
                }
            }
        }, 200, 200);
    }

    public static interface Content {
        public JPanel getMainPanel();

        public String getTitle();

        public void onClose();
    }

    private final Content content;

    private static ImageIcon closeBtnIcon = new ImageIcon(Resource.getImage("closebtn.png", 15, 15));

    private JLabel titleLabel;

    public void addToTabShelf(final JTabbedPane tabPane) {
        final JPanel panel = content.getMainPanel();
        tabPane.add(panel);
        tabPane.insertTab("", null, panel, "", 0);
        int index = tabPane.indexOfComponent(panel);
        JPanel tab = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tab.setOpaque(false);
        tab.add(titleLabel = new JLabel(content.getTitle()));

        JPanel padding = new JPanel();
        padding.setPreferredSize(new Dimension(5, 10));
        padding.setOpaque(false);
        tab.add(padding);

        JButton closeBtn = new JButton();
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setOpaque(false);
        closeBtn.setPreferredSize(new Dimension(15, 15));
        closeBtn.setIcon(closeBtnIcon);
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabPane.remove(panel);
                content.onClose();
                if (tabPane.getTabCount() == 0) {
                    (new TabPage(new WelcomePage())).addToTabShelf(tabPane);
                }
            }
        });
        tab.add(closeBtn);

        tabPane.setTabComponentAt(index, tab);

        tabPane.setSelectedIndex(index);
    }
}

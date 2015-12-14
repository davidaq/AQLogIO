package com.davidaq.logio;

import com.davidaq.logio.Model.LogListModel;
import com.davidaq.logio.Model.RemoteLogConfig;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainUI extends JFrame {
    private JPanel contentPane;
    private JButton addBtn;
    private JTabbedPane tabShelf;
    private JList logList;
    private LogListModel logListModel = new LogListModel();
    private JPopupMenu logListContextMenu = new JPopupMenu();

    public MainUI() {
        setContentPane(contentPane);
        setDefaultLookAndFeelDecorated(true);
        setIconImage(Resource.getImage("icon.png"));
        setTitle("AQ LogIO");
        setupLogListContextMenu();

        addToTabShelf(new WelcomePage());

        logList.setModel(logListModel);
        logList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                check(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                check(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    int index = logList.locationToIndex(e.getPoint());
                    if (logList.getCellBounds(index, index).contains(e.getPoint())) {
                        openLogPage(logListModel.elementAt(index));
                    }
                }
            }

            private void check(MouseEvent e) {
                if (!logListModel.isEmpty() && e.isPopupTrigger()) {
                    int index = logList.locationToIndex(e.getPoint());
                    if (logList.getCellBounds(index, index).contains(e.getPoint())) {
                        logList.setSelectedIndex(index);
                        logListContextMenu.show(logList, e.getX(), e.getY());
                    }
                }
            }
        });

        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RemoteLogConfig logConfig = ConfigRemoteLogDialog.createNew();
                if (logConfig == null)
                    return;
                logListModel.addElement(logConfig);
                logListModel.save();
            }
        });
    }

    public void addToTabShelf(TabPage.Content tabContent) {
        (new TabPage(tabContent)).addToTabShelf(tabShelf);
    }

    private void setupLogListContextMenu() {
        JMenuItem deflt = addLogListContextMenuItem("打开", new ContextMenuAction() {
            @Override
            public void run(int index, RemoteLogConfig item) {
                openLogPage(item);
            }
        });
        Font font = deflt.getFont();
        deflt.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
        addLogListContextMenuItem("修改", new ContextMenuAction() {
            @Override
            public void run(int index, RemoteLogConfig item) {
                ConfigRemoteLogDialog.editExisting(item);
                logListModel.save();
                logListModel.fireUpdate(index);
            }
        });
        logListContextMenu.addSeparator();
        addLogListContextMenuItem("提前", moveItemAction(-1));
        addLogListContextMenuItem("置后", moveItemAction(1));
        addLogListContextMenuItem("置顶", moveItemAction(-0x7fffffff));
        addLogListContextMenuItem("置底", moveItemAction(0x7fffffff));
        logListContextMenu.addSeparator();
        addLogListContextMenuItem("删除", new ContextMenuAction() {
            @Override
            public void run(int index, RemoteLogConfig item) {
                logListModel.remove(index);
                logListModel.save();
            }
        });
    }

    private void openLogPage(RemoteLogConfig logConfig) {
        if (logConfig == null)
            return;
        if (logConfig.ui == null) {
            addToTabShelf(new LogPage(logConfig));
        } else {
            tabShelf.setSelectedComponent(logConfig.ui.getMainPanel());
        }
    }

    private static interface ContextMenuAction {
        public void run(int index, RemoteLogConfig item);
    }

    private ContextMenuAction moveItemAction(final int translate) {
        return new ContextMenuAction() {
            @Override
            public void run(int index, RemoteLogConfig item) {
                logListModel.remove(index);
                index += translate;
                if (index < 0)
                    index = 0;
                if (index >= logListModel.size())
                    index = logListModel.size();
                logListModel.add(index, item);
                logList.setSelectedIndex(index);
                logListModel.save();
            }
        };
    }

    private JMenuItem addLogListContextMenuItem(String name, final ContextMenuAction action) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = logList.getSelectedIndex();
                RemoteLogConfig logConfig = logListModel.elementAt(index);
                if (logConfig == null) {
                    return;
                }
                action.run(index, logConfig);
            }
        });
        logListContextMenu.add(item);
        return item;
    }


    static {
        try {
            UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static final MainUI mainUI = new MainUI();

    public static void main(String[] args) {
        MainUI dialog = mainUI;
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dialog.setVisible(true);
    }
}

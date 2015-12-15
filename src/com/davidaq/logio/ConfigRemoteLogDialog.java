package com.davidaq.logio;

import com.davidaq.logio.model.RemoteLogConfig;

import javax.swing.*;
import java.awt.event.*;
import java.text.NumberFormat;

public class ConfigRemoteLogDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameField;
    private JTextField hostField;
    private JFormattedTextField portField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField keyFileField;
    private JButton keyFileBtn;
    private JTextField pathField;
    private boolean canceled = false;

    private final RemoteLogConfig config;

    public ConfigRemoteLogDialog(RemoteLogConfig config) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("配置远程日志");
        setIconImage(Resource.getImage("configurelog.png"));

        this.config = config;

        nameField.setText(config.name);
        hostField.setText(config.host);
        portField.setText(config.port == null ? "22" : config.port.toString());
        usernameField.setText(config.username);
        passwordField.setText(config.password);
        keyFileField.setText(config.keyFile);
        pathField.setText(config.path);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        config.name = nameField.getText();
        config.host = hostField.getText();
        config.port = Integer.parseInt(portField.getText());
        config.username = usernameField.getText();
        config.password = new String(passwordField.getPassword());
        config.keyFile = keyFileField.getText();
        config.path = pathField.getText();
        dispose();
    }

    private void onCancel() {
        canceled = true;
        dispose();
    }

    public static RemoteLogConfig createNew() {
        RemoteLogConfig config = new RemoteLogConfig();
        config.name = "新建远程日志";
        if (showDialog(config)) {
            return config;
        } else {
            return null;
        }
    }

    public static RemoteLogConfig editExisting(RemoteLogConfig config) {
        showDialog(config);
        return config;
    }

    private static boolean showDialog(RemoteLogConfig config) {
        ConfigRemoteLogDialog dialog = new ConfigRemoteLogDialog(config);
        dialog.pack();
        dialog.setLocationRelativeTo(MainUI.mainUI);
        dialog.setResizable(false);
        dialog.setVisible(true);
        return !dialog.canceled;
    }

    private void createUIComponents() {
        portField = new JFormattedTextField(NumberFormat.getIntegerInstance());
    }
}

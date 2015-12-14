package com.davidaq.logio;

import com.davidaq.logio.Model.RemoteLogConfig;

import javax.swing.*;

public class LogPage implements TabPage.Content {
    private JPanel contentPane;
    private JButton buttonOK;

    private RemoteLogConfig logConfig;

    public LogPage(RemoteLogConfig logConfig) {
        this.logConfig = logConfig;
        logConfig.ui = this;
    }

    @Override
    public JPanel getMainPanel() {
        return contentPane;
    }

    @Override
    public String getTitle() {
        return logConfig.name;
    }

    @Override
    public void onClose() {
        logConfig.ui = null;
    }
}

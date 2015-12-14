package com.davidaq.logio;

import javax.swing.*;

public class WelcomePage implements TabPage.Content {
    private JPanel panel;
    private JLabel label;

    @Override
    public JPanel getMainPanel() {
        return panel;
    }

    @Override
    public String getTitle() {
        return "欢迎";
    }

    @Override
    public void onClose() {
    }
}

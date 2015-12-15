package com.davidaq.logio;

import com.davidaq.logio.model.LogInput;
import com.davidaq.logio.model.RemoteLogConfig;
import com.davidaq.logio.model.RemoteLogInput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;

public class LogPage implements TabPage.Content {
    private JPanel contentPane;
    private JScrollBar vScroll;
    private JTextPane textPane;
    private JPanel textWrapPanel;
    private JScrollPane scrollPane;

    private RemoteLogConfig logConfig;
    private LogInput input;

    private int currentPos = 0;
    private int lastPos = 0;
    private int sVal = 0;
    private long updateTimestamp;

    public LogPage(RemoteLogConfig logConfig) {
        this.logConfig = logConfig;
        logConfig.ui = this;

        input = new RemoteLogInput(logConfig);

        textWrapPanel.setLayout(null);
        textWrapPanel.setOpaque(false);
        textPane.setOpaque(false);

        vScroll.setMaximum(100);
        vScroll.setValue(100);

        contentPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                scrollPane.move(0, 0);
                scrollPane.resize(textWrapPanel.getWidth(), textWrapPanel.getHeight());
                updateUI();
            }
        });
        textPane.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                currentPos += e.getWheelRotation();
                updateUI(true);
            }
        });
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateUI();
            }
        }, 150, 150);
    }

    private void updateUI() {
        updateUI(false);
    }

    private void updateUI(boolean forceUsePos) {
        FontMetrics metrics = textPane.getFontMetrics(textPane.getFont());
        int lineHeight = metrics.getHeight();
        int visibleHeight = scrollPane.getViewport().getHeight() - 5;
        int visibleLines = visibleHeight / lineHeight;
        int lineCount = input.getLineCount();
        int row;
        boolean isBottom = false;
        if (forceUsePos) {
            row = currentPos;
        } else if (vScroll.getValue() >= vScroll.getMaximum() - vScroll.getBlockIncrement()) {
            row = lineCount;
            isBottom = true;
        } else if (sVal != vScroll.getValue()) {
            sVal = vScroll.getValue();
            row = vScroll.getValue() * (lineCount - visibleLines);
            row = row / vScroll.getMaximum() + (row % vScroll.getMaximum() > 0 ? 1 : 0);
        } else {
            row = currentPos;
        }
        int lastVisibleLine = row + visibleLines - 1;
        if (lastVisibleLine >= lineCount) {
            lastVisibleLine = lineCount - 1;
            row = lastVisibleLine - visibleLines + 1;
        }
        if (row < 0)
            row = 0;
        long ts = input.getUpdateTimestamp();
        if (ts <= updateTimestamp && currentPos == row && lastPos == lastVisibleLine)
            return;
        updateTimestamp = ts;
        currentPos = row;
        lastPos = lastVisibleLine;
        StringBuilder text = new StringBuilder();
        for (int i = row; i <= lastVisibleLine; i++) {
            String line = input.getLineAt(i);
            text.append(line);
            if (i < lastVisibleLine)
                text.append('\n');
        }
        String str = text.toString();
        textPane.setText(str);
        textPane.setSelectionStart(0);
        textPane.setSelectionEnd(0);
        int vMax = lineCount - visibleLines;
        if (vMax < 1) {
            vScroll.setValue(0);
            vScroll.setMaximum(1);
        } else {
            if (vMax > 10)
                vMax = 10;
            vScroll.setMaximum(vMax);
            if (isBottom) {
                vScroll.setValue(vMax - vScroll.getBlockIncrement());
            } else if (forceUsePos) {
                sVal = row * (vMax - vScroll.getBlockIncrement()) / lineCount;
                vScroll.setValue(sVal);
            }
        }
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
        input.close();
    }
}

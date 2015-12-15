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
    private String text = "";

    public LogPage(RemoteLogConfig logConfig) {
        this.logConfig = logConfig;
        logConfig.ui = this;

        input = new RemoteLogInput(logConfig);
        //input = new TestLogInput();

        textWrapPanel.setLayout(null);
        textWrapPanel.setOpaque(false);
        textPane.setOpaque(false);

        vScroll.setVisibleAmount(100);
        vScroll.setMaximum(100);
        vScroll.setValue(0);

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

    private long lastUpdate = 0;

    private void updateUI(boolean forceUsePos) {
        long now = System.currentTimeMillis();
        if (now - lastUpdate < 50) {
            return;
        }
        lastUpdate = now;
        FontMetrics metrics = textPane.getFontMetrics(textPane.getFont());
        int lineHeight = metrics.getHeight();
        int visibleHeight = scrollPane.getViewport().getHeight() - 5;
        int visibleLines = visibleHeight / lineHeight;
        int lineCount = input.getLineCount();
        int row;
        boolean isBottom = false;
        if (forceUsePos) {
            row = currentPos;
        } else if (vScroll.getValue() >= vScroll.getMaximum() - vScroll.getVisibleAmount()) {
            row = lineCount;
            isBottom = true;
        } else if (sVal != vScroll.getValue()) {
            sVal = vScroll.getValue();
            row = divideInc(vScroll.getValue() * lineCount, vScroll.getMaximum());
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
        if (!str.equals(this.text)) {
            this.text = str;
            textPane.setText(str);
            textPane.setSelectionStart(0);
            textPane.setSelectionEnd(0);
        }
        int vMax = lineCount - visibleLines;
        if (vMax < 1) {
            vScroll.setValues(0, 100, 0, 100);
        } else {
            if (vMax > 100)
                vMax = 100;
            int vExt = divideInc(visibleLines * vMax, lineCount);
            int sVal;
            if (!forceUsePos && isBottom) {
                sVal = vMax - vExt;
            } else {
                if (forceUsePos && lastVisibleLine + 1 >= lineCount) {
                    sVal = vMax - vExt;
                } else {
                    sVal = row * vMax / lineCount;
                    if (sVal >= vMax - vExt) {
                        sVal = vMax - vExt - 1;
                    }
                }
                this.sVal = sVal;
            }
            vScroll.setValues(sVal, vExt, 0, vMax);
        }
    }

    private int divideInc(int a, int b) {
        int ret = a / b;
        if (a % b > 0)
            ret++;
        return ret;
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

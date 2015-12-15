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

    private long currentPos = 0;
    private long lastPos = 0;
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
        long lineHeight = metrics.getHeight();
        long visibleHeight = scrollPane.getViewport().getHeight() - 5;
        long visibleLines = visibleHeight / lineHeight;
        long lineCount = input.getLineCount();
        long row;
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
        long lastVisibleLine = row + visibleLines - 1;
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
        for (long i = row; i <= lastVisibleLine; i++) {
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
        long vMax = lineCount - visibleLines;
        if (vMax < 1) {
            vScroll.setValues(0, 100, 0, 100);
        } else {
            if (vMax > 5000)
                vMax = 5000;
            long vExt = divideInc(visibleLines * vMax, lineCount);
            if (vExt < vMax / 12) {
                vExt = vMax / 12;
            }
            long sVal;
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
                this.sVal = (int) sVal;
            }
            vScroll.setValues((int) sVal, (int) vExt, 0, (int) vMax);
        }
    }

    private long divideInc(long a, long b) {
        long ret = a / b;
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

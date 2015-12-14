package com.davidaq.logio;

import com.davidaq.logio.Model.LogInput;
import com.davidaq.logio.Model.RemoteLogConfig;
import com.davidaq.logio.Model.TestLogInput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.Timer;

public class LogPage implements TabPage.Content {
    private JPanel contentPane;
    private JScrollBar vscroll;
    private JTextPane textPane;
    private JPanel textWrapPanel;
    private JScrollPane scrollPane;

    private RemoteLogConfig logConfig;
    private LogInput input;

    private int currentPos = 0;
    private int lastPos = 0;
    private int sval = 0;
    private long updateTimestamp;

    public LogPage(RemoteLogConfig logConfig) {
        this.logConfig = logConfig;
        logConfig.ui = this;

        input = new TestLogInput();

        textWrapPanel.setLayout(null);
        textWrapPanel.setOpaque(false);
        textPane.setOpaque(false);

        vscroll.setMaximum(100);
        vscroll.setValue(100);

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
        int visibleHeight = scrollPane.getViewport().getHeight();
        int visibleLines = visibleHeight / lineHeight;
        int row;
        boolean isBottom = false;
        if (forceUsePos) {
            row = currentPos;
        } else if (vscroll.getValue() >= vscroll.getMaximum() - vscroll.getBlockIncrement()) {
            row = input.getLineCount();
            isBottom = true;
        } else if (sval != vscroll.getValue()) {
            sval = vscroll.getValue();
            row = vscroll.getValue() * (input.getLineCount() - visibleLines);
            row = row / vscroll.getMaximum() + (row % vscroll.getMaximum() > 0 ? 1 : 0);
        } else {
            row = currentPos;
        }
        int lastVisibleLine = row + visibleLines - 1;
        if (lastVisibleLine >= input.getLineCount()) {
            lastVisibleLine = input.getLineCount() - 1;
            row = lastVisibleLine - visibleLines + 1;
        }
        if (row < 0)
            row = 0;
        if (updateTimestamp < input.getUpdateTimestamp() && currentPos == row && lastPos == lastVisibleLine)
            return;
        updateTimestamp = input.getUpdateTimestamp();
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
        int vMax = input.getLineCount() - visibleLines;
        if (vMax < 1) {
            vscroll.setValue(0);
            vscroll.setMaximum(1);
        } else {
            if (vMax > 10)
                vMax = 10;
            vscroll.setMaximum(vMax);
            if (isBottom) {
                vscroll.setValue(vMax - vscroll.getBlockIncrement());
            } else if (forceUsePos) {
                sval = row * (vMax - vscroll.getBlockIncrement()) / input.getLineCount();
                vscroll.setValue(sval);
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

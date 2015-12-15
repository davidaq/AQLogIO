package com.davidaq.logio.model;

import java.util.Timer;
import java.util.TimerTask;

public class TestLogInput implements LogInput {
    private int N = 0;

    public TestLogInput() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                N++;
            }
        }, 100, 100);
    }

    @Override
    public long getLineCount() {
        return N;
    }

    @Override
    public String getLineAt(long lineNum) {
        return "----------------------------------------------------------------------------------" + lineNum;
    }

    @Override
    public long getUpdateTimestamp() {
        return N;
    }

    @Override
    public void close() {
    }
}

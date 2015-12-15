package com.davidaq.logio.model;

public interface LogInput {
    public int getLineCount();

    public String getLineAt(int lineNum);

    public long getUpdateTimestamp();

    public void close();
}

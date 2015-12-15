package com.davidaq.logio.model;

public interface LogInput {
    public long getLineCount();

    public String getLineAt(long lineNum);

    public long getUpdateTimestamp();

    public void close();
}

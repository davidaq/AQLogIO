package com.davidaq.logio.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EncodedInputStream extends InputStream {
    private final InputStream source;

    public EncodedInputStream(InputStream source) {
        this.source = new BufferedInputStream(source);
    }

    @Override
    public int read() throws IOException {
        int b = source.read();
        if (b == -1)
            return -1;
        b ^= 0xff;
        return b;
    }

    @Override
    public void close() throws IOException {
        source.close();
    }
}

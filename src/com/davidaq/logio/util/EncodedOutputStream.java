package com.davidaq.logio.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class EncodedOutputStream extends OutputStream {
    private final OutputStream target;

    public EncodedOutputStream(OutputStream target) {
        this.target = new BufferedOutputStream(target);
    }

    @Override
    public void write(int b) throws IOException {
        b += 128;
        b ^= 0xff;
        b -= 128;
        target.write(b);
    }

    @Override
    public void close() throws IOException {
        target.close();
    }

    @Override
    public void flush() throws IOException {
        target.flush();
    }
}

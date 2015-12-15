package com.davidaq.logio.model;

import com.davidaq.logio.util.EncodedInputStream;
import com.davidaq.logio.util.EncodedOutputStream;

import javax.swing.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Timer;
import java.util.TimerTask;

public class LogListModel extends DefaultListModel<RemoteLogConfig> {
    private final File storageFile = new File(new File(System.getProperty("user.home")), "AQLogIO.dat");
    private final File lockFile = new File(new File(System.getProperty("user.home")), "AQLogIO.lck");

    private long loadTime = 0;

    public LogListModel() {
        load();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (storageFile.lastModified() > loadTime) {
                    load();
                }
            }
        }, 500L, 500L);
    }

    public void save() {
        try {
            obtainLock();
            try {
                OutputStream out = new FileOutputStream(storageFile);
                try {
                    out = new EncodedOutputStream(out);
                    ObjectOutputStream oOut = new ObjectOutputStream(out);
                    oOut.writeInt(getSize());
                    for (int i = 0; i < getSize(); i++) {
                        oOut.writeObject(get(i));
                    }
                } finally {
                    out.close();
                }
                storageFile.setLastModified(System.currentTimeMillis());
                loadTime = System.currentTimeMillis();
            } finally {
                releaseLock();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (!storageFile.exists())
            return;
        loadTime = System.currentTimeMillis();
        clear();
        try {
            obtainLock();
            try {
                InputStream in = new FileInputStream(storageFile);
                try {
                    in = new EncodedInputStream(in);
                    ObjectInputStream oIn = new ObjectInputStream(in);
                    try {
                        int size = oIn.readInt();
                        for (int i = 0; i < size; i++) {
                            addElement((RemoteLogConfig) oIn.readObject());
                        }
                    } finally {
                        oIn.close();
                    }
                } finally {
                    in.close();
                }
            } finally {
                releaseLock();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private FileChannel lockChannel;
    private FileLock lock;

    private void obtainLock() throws IOException {
        lockChannel = new RandomAccessFile(lockFile, "rw").getChannel();
        lock = lockChannel.lock();
    }

    private void releaseLock() throws IOException {
        lock.release();
        lockChannel.close();
        lockFile.delete();
    }

    public void fireUpdate(int index) {
        fireContentsChanged(this, index, index);
    }
}

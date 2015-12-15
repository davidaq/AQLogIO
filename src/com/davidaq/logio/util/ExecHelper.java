package com.davidaq.logio.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.*;

public class ExecHelper {
    private final Session session;
    private final String charset;

    public ExecHelper(Session session, String charset) {
        this.session = session;
        this.charset = charset;
    }

    public int exec(String command, StringBuilder output) {
        try {
            System.out.println(command);
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(null);
            InputStream in = channel.getInputStream();
            channel.connect();
            ByteArrayOutputStream pool = new ByteArrayOutputStream(1005);
            byte buff[] = new byte[1005];
            boolean end = false;
            while (!end) {
                while (in.available() > 0) {
                    int len = in.read(buff, 0, 1000);
                    if (len <= 0) {
                        break;
                    }
                    pool.write(buff, 0, len);
                }
                if (channel.isEOF() && channel.isClosed()) {
                    if (in.available() > 0)
                        continue;
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            byte[] bout = pool.toByteArray();
            output.append(new String(bout, 0, bout.length, charset));
            return channel.getExitStatus();
        } catch (JSchException | IOException e) {
            e.printStackTrace();
            return -99999;
        }
    }
}

package com.davidaq.logio.Model;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExecHelper {
    private final Session session;

    public ExecHelper(Session session) {
        this.session = session;
    }

    public int exec(String command, StringBuilder output) {
        try {
            System.out.println(command);
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(null);
            InputStream in = new BufferedInputStream(channel.getInputStream());
            InputStreamReader reader = new InputStreamReader(in);
            channel.connect();
            char buff[] = new char[400];
            while (true) {
                while (in.available() > 0) {
                    int len = reader.read(buff);
                    if (len < 0)
                        break;
                    output.append(buff, 0, len);
                }
                if (channel.isClosed()) {
                    if (in.available() > 0)
                        continue;
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return channel.getExitStatus();
        } catch (JSchException | IOException e) {
            e.printStackTrace();
            return -99999;
        }
    }
}

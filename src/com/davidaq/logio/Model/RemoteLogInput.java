package com.davidaq.logio.Model;

import com.jcraft.jsch.*;
import com.sun.corba.se.impl.interceptors.InterceptorInvoker;
import com.sun.corba.se.spi.orbutil.fsm.Input;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RemoteLogInput implements LogInput {
    private final RemoteLogConfig config;
    JSch ssh;
    Session session;
    ExecHelper helper;

    public RemoteLogInput(RemoteLogConfig config) {
        this.config = config;
        ssh = new JSch();
        try {
            session = ssh.getSession(config.username, config.host, config.port);
            session.setPassword(config.password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            helper = new ExecHelper(session);
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    int n = -1;

    @Override
    public int getLineCount() {
        if (n > 0)
            return n;
        StringBuilder output = new StringBuilder();
        if (0 == helper.exec("wc -l '" + config.path + "'", output)) {
            String num = output.toString().split(" ")[0];
            n = Integer.parseInt(num);
            return Integer.parseInt(num);
        }
        return 0;
    }

    @Override
    public String getLineAt(int lineNum) {
        StringBuilder output = new StringBuilder();
        lineNum++;
        if (0 == helper.exec("sed -n -e '" + lineNum + "," + lineNum + "p' '" + config.path + "'", output)) {
            String ret = output.toString();
            return ret.replaceAll("\r?\n\r?$", "");
        }
        return "";
    }

    @Override
    public long getUpdateTimestamp() {
        return 0;
    }

    @Override
    public void close() {
        session.disconnect();
    }
}

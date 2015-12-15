package com.davidaq.logio.model;

import com.apple.eawt.AppEvent;
import com.davidaq.logio.util.ExecHelper;
import com.davidaq.logio.util.IntegerCombineQueue;
import com.davidaq.logio.util.LimitedCache;
import com.davidaq.logio.util.QueueConsumer;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class RemoteLogInput implements LogInput, QueueConsumer.ConsumeLogic<IntegerCombineQueue.Block> {
    private final RemoteLogConfig config;
    private JSch ssh;
    private Session session;
    private ExecHelper helper;
    private IntegerCombineQueue getLineQueue = new IntegerCombineQueue();
    private QueueConsumer<IntegerCombineQueue.Block> consumer = new QueueConsumer<>(getLineQueue, this);
    private String statusMessage = "正在连接服务器";

    public RemoteLogInput(RemoteLogConfig config) {
        this.config = config;
        consumer.start();
        getLineQueue.push(-20);
    }

    private volatile int lineCount = 0;

    @Override
    public int getLineCount() {
        if (consumer.isIdle()) {
            getLineQueue.push(-10);
        }
        if (statusMessage != null)
            return 1;
        return lineCount;
    }


    private final LimitedCache<Integer, String> linesCache = new LimitedCache<>(250);

    @Override
    public String getLineAt(int lineNum) {
        if (lineNum < 0)
            return "";
        if (statusMessage != null && lineNum == 0)
            return statusMessage;
        synchronized (linesCache) {
            String line = linesCache.get(lineNum);
            if (line != null) {
                return line;
            }
        }
        getLineQueue.push(lineNum);
        return "";
    }

    private long timestamp = 0;

    @Override
    public long getUpdateTimestamp() {
        synchronized (this) {
            return timestamp;
        }
    }

    @Override
    public void close() {
        consumer.stop();
        session.disconnect();
    }

    @Override
    public void consume(IntegerCombineQueue.Block value) {
        if (value.start == -20) {
            try {
                ssh = new JSch();
                session = ssh.getSession(config.username, config.host, config.port);
                session.setPassword(config.password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                helper = new ExecHelper(session, config.charset);
                statusMessage = "开始获取日志数据";
            } catch (JSchException e) {
                statusMessage = "连接失败";
                if (e.getMessage().matches("Auth fail")) {
                    statusMessage += ": 登录失败";
                }
                e.printStackTrace();
            }
        } else if (value.start == -10) {
            StringBuilder output = new StringBuilder();
            if (0 == helper.exec("wc -l '" + config.path + "'", output)) {
                String num = output.toString().split(" ")[0];
                try {
                    int val = Integer.parseInt(num);
                    if (lineCount == val)
                        return;
                    lineCount = val;
                    statusMessage = null;
                } catch (NumberFormatException e) {
                    statusMessage = "未知服务器响应信息";
                }
            } else {
                statusMessage = "日志文件不可读";
            }
        } else {
            StringBuilder output = new StringBuilder();
            int start = value.start + 1;
            int end = value.start + value.length;
            if (0 == helper.exec("sed -n -e '" + start + "," + end + "p' '" + config.path + "'", output)) {
                String ret = output.toString();
                String[] lines = ret.split("\r?\n\r?");
                synchronized (linesCache) {
                    for (int i = 0; i < value.length && i < lines.length; i++) {
                        linesCache.put(value.start + i, lines[i]);
                    }
                }
            } else {
                return;
            }
        }
        synchronized (this) {
            timestamp++;
        }
    }
}

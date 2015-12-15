package com.davidaq.logio.model;

import com.davidaq.logio.LogPage;

import java.io.Serializable;

public class RemoteLogConfig implements Serializable {
    public String name;
    public String host;
    public Integer port;
    public String path;
    public String username;
    public String password;
    public String keyFile;
    public String charset;

    public transient LogPage ui;

    public RemoteLogConfig() {
    }

    public RemoteLogConfig(RemoteLogConfig item) {
        name = item.name;
        host = item.host;
        port = item.port;
        path = item.path;
        username = item.username;
        password = item.password;
        keyFile = item.keyFile;
        charset = item.charset;
    }

    @Override
    public String toString() {
        return name == null || name.isEmpty() ? "未命名" : name;
    }
}

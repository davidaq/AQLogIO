package com.davidaq.logio.Model;

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

    public transient LogPage ui;

    @Override
    public String toString() {
        return name == null || name.isEmpty() ? "未命名" : name;
    }
}

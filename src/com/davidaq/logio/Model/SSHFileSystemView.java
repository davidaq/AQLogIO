package com.davidaq.logio.model;

import com.davidaq.logio.util.ExecHelper;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sun.xml.internal.bind.annotation.OverrideAnnotationOf;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SSHFileSystemView extends FileSystemView {
    private ExecHelper helper;
    private Session session;

    private String home;

    public SSHFileSystemView(String host, Integer port, String username, String password, String keyFile) throws JSchException {
        JSch ssh = new JSch();
        if (keyFile != null && !keyFile.isEmpty()) {
            ssh.addIdentity(keyFile);
        }
        session = ssh.getSession(username, host, port);
        if (password != null && !password.isEmpty()) {
            session.setPassword(password);
        }
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        helper = new ExecHelper(session, "UTF-8");
        StringBuilder result = new StringBuilder();
        helper.exec("pwd", result);
        home = result.toString();
    }

    static class MarkedFile extends File {
        public MarkedFile(String pathname, boolean dir) {
            super(pathname);
            isDir = dir;
        }

        @Override
        public boolean isDirectory() {
            return isDir;
        }

        @Override
        public boolean isHidden() {
            return getName().startsWith(".");
        }

        boolean isDir = false;
    }

    public void close() {
        session.disconnect();
    }

    @Override
    public boolean isRoot(File f) {
        return f.getAbsolutePath().equals("/");
    }

    @Override
    public Boolean isTraversable(File f) {
        return f.isDirectory();
    }

    //@Override
    //public String getSystemDisplayName(File f) {
    //    return super.getSystemDisplayName(f);
    //}

    //@Override
    //public String getSystemTypeDescription(File f) {
    //    return super.getSystemTypeDescription(f);
    //}

    //@Override
    //public Icon getSystemIcon(File f) {
    //    return super.getSystemIcon(f);
    //}

    @Override
    public boolean isParent(File folder, File file) {
        return new File(folder, file.getName()).getAbsolutePath().equals(file.getAbsolutePath());
    }

    @Override
    public File getChild(File parent, String fileName) {
        return new File(parent, fileName);
    }

    @Override
    public boolean isFileSystem(File f) {
        return true;
    }

    @Override
    public boolean isHiddenFile(File f) {
        return f.getName().startsWith(".");
    }

    @Override
    public boolean isFileSystemRoot(File dir) {
        return isRoot(dir);
    }

    @Override
    public boolean isDrive(File dir) {
        return false;
    }

    @Override
    public boolean isFloppyDrive(File dir) {
        return false;
    }

    @Override
    public boolean isComputerNode(File dir) {
        return false;
    }

    @Override
    public File[] getRoots() {
        return new File[]{new MarkedFile(home, true), new MarkedFile("/", true)};
    }

    @Override
    public File getHomeDirectory() {
        System.out.println(home);
        return new MarkedFile(home, true);
    }

    @Override
    public File getDefaultDirectory() {
        return getHomeDirectory();
    }

    @Override
    public File createFileObject(File dir, String filename) {
        return new File(dir, filename);
    }

    @Override
    public File createFileObject(String path) {
        return new File(path);
    }

    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {
        StringBuilder result = new StringBuilder();
        helper.exec("ls '" + dir.getAbsolutePath() + "' -F -1", result);
        String items[] = result.toString().split("\n");
        ArrayList<File> ret = new ArrayList<>();
        for (String item : items) {
            if (item.isEmpty()) {
                continue;
            }
            if (useFileHiding && item.startsWith(".")) {
                continue;
            }
            boolean isDir = item.endsWith("/");
            item = item.replaceAll("[\\/\\*\\@\\=\\%\\|]$", "");
            ret.add(new MarkedFile(new File(dir, item).getAbsolutePath(), isDir));
        }
        return ret.toArray(new File[ret.size()]);
    }

    @Override
    public File getParentDirectory(File dir) {
        if (dir == null || dir.getParentFile() == null)
            return null;
        return new MarkedFile(dir.getParentFile().getAbsolutePath(), true);
    }

    @Override
    protected File createFileSystemRoot(File f) {
        return new MarkedFile(f.getAbsolutePath(), true);
    }

    @Override
    public File createNewFolder(File containingDir) throws IOException {
        return new File(containingDir, "New Directory");
    }
}

package com.davidaq.logio.model;

import com.davidaq.logio.util.ExecHelper;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FilenameFilter;
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
        home = result.toString().split("\r?\n")[0];
    }

    class MarkedFile extends File {
        public MarkedFile(File file, String name) {
            this(new File(file, name));
        }

        public MarkedFile(File file, String name, boolean dir) {
            this(new File(file, name), dir);
        }

        public MarkedFile(File file) {
            this(file, false);
        }

        public MarkedFile(File file, boolean dir) {
            this(file.getAbsolutePath(), dir);
        }

        public MarkedFile(String pathname) {
            this(pathname, false);
        }

        public MarkedFile(String pathname, boolean dir) {
            super(pathname);
            isDir = dir;
            hidden = getName().startsWith(".");
        }

        @Override
        public boolean isDirectory() {
            return isDir;
        }

        @Override
        public boolean isHidden() {
            return getName().startsWith(".");
        }

        @Override
        public File[] listFiles() {
            StringBuilder result = new StringBuilder();
            helper.exec("ls '" + getAbsolutePath() + "' -F -1", result);
            String items[] = result.toString().split("\n");
            ArrayList<File> ret = new ArrayList<>();
            for (String item : items) {
                if (item.isEmpty()) {
                    continue;
                }
                boolean isDir = item.endsWith("/");
                item = item.replaceAll("[\\/\\*\\@\\=\\%\\|]$", "");
                ret.add(new MarkedFile(new File(this, item), isDir));
            }
            return ret.toArray(new File[ret.size()]);
        }

        @Override
        public String[] list() {
            File[] files = listFiles();
            String[] ret = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                ret[i] = files[i].getName();
            }
            return ret;
        }

        @Override
        public File[] listFiles(FilenameFilter filter) {
            return listFiles();
        }

        @Override
        public String[] list(FilenameFilter filter) {
            return list();
        }

        @Override
        public long getTotalSpace() {
            return 0;
        }

        @Override
        public long getFreeSpace() {
            return 0;
        }

        @Override
        public long getUsableSpace() {
            return 0;
        }

        @Override
        public boolean isFile() {
            return !isDir;
        }

        boolean isDir = false;
        boolean hidden = false;

        @Override
        public boolean renameTo(File dest) {
            return false;
        }

        @Override
        public boolean setLastModified(long time) {
            return false;
        }

        @Override
        public boolean setReadOnly() {
            return false;
        }

        @Override
        public boolean setWritable(boolean writable, boolean ownerOnly) {
            return false;
        }

        @Override
        public boolean setWritable(boolean writable) {
            return false;
        }

        @Override
        public boolean setReadable(boolean readable, boolean ownerOnly) {
            return false;
        }

        @Override
        public boolean setReadable(boolean readable) {
            return false;
        }

        @Override
        public boolean setExecutable(boolean executable, boolean ownerOnly) {
            return false;
        }

        @Override
        public boolean setExecutable(boolean executable) {
            return false;
        }

        @Override
        public boolean delete() {
            return false;
        }

        @Override
        public void deleteOnExit() {
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public boolean canWrite() {
            return true;
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public boolean canExecute() {
            return true;
        }

        @Override
        public long lastModified() {
            return System.currentTimeMillis();
        }

        @Override
        public long length() {
            return 0;
        }

        @Override
        public boolean createNewFile() throws IOException {
            return false;
        }

        @Override
        public boolean mkdir() {
            return false;
        }

        @Override
        public boolean mkdirs() {
            return false;
        }

        @Override
        public File getParentFile() {
            File p = super.getParentFile();
            if (p == null)
                return null;
            return new MarkedFile(p, true);
        }
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
        System.out.println(f.getAbsolutePath() + " : " + (f instanceof MarkedFile));
        return true;
    }

    //@Override
    //public String getSystemDisplayName(File f) {
    //    return super.getSystemDisplayName(f);
    //}

    @Override
    public String getSystemTypeDescription(File f) {
        return super.getSystemTypeDescription(f);
    }

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
        return new MarkedFile(new File(parent, fileName), false);
    }

    @Override
    public boolean isFileSystem(File f) {
        return false;
    }

    @Override
    public boolean isHiddenFile(File f) {
        return f.isHidden();
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
        return new MarkedFile(home, true);
    }

    @Override
    public File getDefaultDirectory() {
        return getHomeDirectory();
    }

    @Override
    public File createFileObject(File dir, String filename) {
        return new MarkedFile(dir, filename, false);
    }

    @Override
    public File createFileObject(String path) {
        return new MarkedFile(path, false);
    }

    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {
        return dir.listFiles();
    }

    @Override
    public File getParentDirectory(File dir) {
        if (dir == null)
            return null;
        return dir.getParentFile();
    }

    @Override
    protected File createFileSystemRoot(File f) {
        return new MarkedFile(f.getAbsolutePath(), true);
    }

    @Override
    public File createNewFolder(File containingDir) throws IOException {
        return new MarkedFile(new File(containingDir, "New Directory").getAbsolutePath(), true);
    }
}

package com.core.contant;

/**
 * Created by Thinkpad on 2018/1/14 19:37.
 */
public class FileModel {

    private String name;
    private boolean dir;
    private long lastModified;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDir() {
        return dir;
    }

    public void setDir(boolean dir) {
        this.dir = dir;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}

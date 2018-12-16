package com.youme.entity;

/**
 * 文件传输model
 * Created by leihtg on 2018/12/1 14:16.
 */
public class FileTransfer {

    private String path;
    private long pos;
    private boolean dir;
    private int perSecondLen;
    private long length;
    private long lastModified;
    private FileTransferType flags;


    public FileTransfer() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getPos() {
        return pos;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public FileTransferType getFlags() {
        return flags;
    }

    public void setFlags(FileTransferType flags) {
        this.flags = flags;
    }

    public int getPerSecondLen() {
        return perSecondLen;
    }

    public void setPerSecondLen(int perSecondLen) {
        this.perSecondLen = perSecondLen;
    }

    public boolean isDir() {
        return dir;
    }

    public void setDir(boolean dir) {
        this.dir = dir;
    }
}

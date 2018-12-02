package com.youme.entity;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件传输model
 * Created by leihtg on 2018/12/1 14:16.
 */
public class FileTransfer {
    private static ConcurrentHashMap<FileTransfer, String> list = new ConcurrentHashMap<>();

    private String name;
    private long pos;
    private long prePos;
    private int perSecondLen;
    private long length;
    private long lastModified;
    private FileTransferType flags;

    static Thread thread = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    Iterator<FileTransfer> it = list.keySet().iterator();
                    while (it.hasNext()) {
                        FileTransfer ft = it.next();
                        if (ft.getFlags() == FileTransferType.OVER) {
                            it.remove();
                            continue;
                        }
                        ft.perSecondLen = (int) (ft.pos - ft.prePos);
                        ft.prePos = ft.pos;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static boolean isStart = false;

    public FileTransfer() {
        list.put(this, "");
        if (!isStart) {
            isStart = true;
            thread.start();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}

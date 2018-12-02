package com.youme.entity;

/**
 * Created by leihtg on 2018/12/1 14:23.
 */
public enum FileTransferType {
    WAITUPLOAD(0, "等待上传"),

    UPLOADING(1, "上传中"),

    WAITDOWNLOAD(2, "等待下载"),

    DOWNLOADING(3, "下载中"),

    OVER(4, "完成");

    int flag;
    String name;

    FileTransferType(int flag, String name) {
        this.flag = flag;
        this.name = name;
    }

    public int getFlag() {
        return flag;
    }

    public String getName() {
        return name;
    }
}

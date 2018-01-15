package com.core.contant;

/**
 * 文件查询时的参数
 * Created by Thinkpad on 2018/1/14 19:11.
 */
public class FileParam {
    private byte msgType;
    private String path;

    public byte getMsgType() {
        return msgType;
    }

    public void setMsgType(byte msgType) {
        this.msgType = msgType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

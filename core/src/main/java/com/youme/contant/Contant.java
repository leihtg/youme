package com.youme.contant;

/**
 * Created by leihuating on 2018/1/8.
 */

public class Contant {
    // 本地云盘目录
    public static final String HOME_DIR = "F:\\myCloud";
    // 广播端口,客户端/服务端各自监听自己本地的端口号
    public static final int BROCAST_PORT = 7777;
    // 请求主机地址信息
    public static final byte REQ_HOST_MSG = 0x01;
    // 响应
    public static final byte RESP_HOST_MSG = 0x02;
    // 服务端监听端口
    public static final int SERVER_PORT = 8877;
    // 获取目录
    public static final byte FETCH_DIR = 0x03;

}

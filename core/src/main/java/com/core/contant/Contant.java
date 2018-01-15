package com.core.contant;

/**
 * Created by leihuating on 2018/1/8.
 */

public class Contant {
    // 本地云盘目录
    public static final String HOME_DIR = "D:\\myCloud";
    //版本
    public static final int VERSION = 1;
    // 服务端监听端口
    public static final int SERVER_PORT = 8877;
    // 广播端口,客户端/服务端各自监听自己本地的端口号
    public static final int BROCAST_PORT = 7777;
    // 请求主机地址信息
    public static final byte REQ_HOST_MSG = 0x01;
    // 响应
    public static final byte FIND_HOST_ADDR_MSG = 0x02;
    //查询host超时
    public static final byte FIND_HOST_ADDR_TIMEOUT = 0x04;
    //连接服务器成功
    public static final byte CONN_HOST_SUCC = 0x05;
    //连接服务器失败
    public static final byte CONN_HOST_FAIL = 0x06;
}

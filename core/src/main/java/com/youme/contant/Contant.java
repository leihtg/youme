package com.youme.contant;

/**
 * Created by leihuating on 2018/1/8.
 */

public class Contant {
    // 广播端口,客户端/服务端各自监听自己本地的端口号
    public static final int BROCAST_PORT = 7777;
    // 手机端端口号
    public static final int CLIENT_PORT = 7788;
    // 请求主机地址信息
    public static final byte REQ_HOST_MSG = 0x01;
    //响应
    public static final byte RESP_HOST_MSG = 0x02;
}

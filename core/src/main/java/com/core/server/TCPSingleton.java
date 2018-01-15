package com.core.server;

import android.os.Handler;
import android.os.Message;

import com.core.contant.Contant;
import com.core.util.JSONUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * create at 2018年1月8日17:39:49
 */
public class TCPSingleton {
    private int count;//重试次数

    private TCPClient tcpClient;

    public Handler connHandler;
    public Handler receiveDataHandler;
    public Handler sendFailHandler;

    public boolean hasFindHostAddress = false;

    private TCPSingleton() {
        receiveBrocast();
        brocastLocalHost();
    }

    private static TCPSingleton singleton = new TCPSingleton();

    public static TCPSingleton getInstance() {
        return singleton;
    }

    /**
     * 连接服务器
     */
    private void connectServer(InetAddress hostAddr) {
        tcpClient = new TCPClient(hostAddr, connHandler, receiveDataHandler, sendFailHandler);
        if (null != tcpClient) {
            tcpClient.connect();
        }
    }

    public void sendData(int type, Object obj) {
        if (null != tcpClient) {
            tcpClient.send(type, JSONUtil.toJson(obj));
        }
    }

    //发送广播
    public void brocastLocalHost() {
        new Thread() {
            @Override
            public void run() {
                while (!hasFindHostAddress) {
                    try {
                        //发送受限广播,同一个局域网内可以接收到
                        InetAddress address = InetAddress.getByName("255.255.255.255");
                        byte[] hostMsg = new byte[]{Contant.REQ_HOST_MSG};
                        DatagramPacket packet = new DatagramPacket(hostMsg, hostMsg.length, address, Contant.BROCAST_PORT);

                        DatagramSocket ds = new DatagramSocket();
                        ds.setBroadcast(true);
                        ds.send(packet);

                        Thread.sleep(3000);
                        if (count++ > 10) {//重试10次
                            Message msg = new Message();
                            msg.what = Contant.FIND_HOST_ADDR_TIMEOUT;
                            connHandler.sendMessage(msg);
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    //接收广播
    private void receiveBrocast() {
        new Thread() {
            @Override
            public void run() {
                try {
                    DatagramSocket ds = new DatagramSocket(Contant.BROCAST_PORT);
                    ds.setBroadcast(true);
                    while (true) {
                        byte[] buf = new byte[2048];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        ds.receive(packet);
                        // 接收的数据
                        buf = Arrays.copyOf(packet.getData(), packet.getLength());
                        if (null != buf) {
                            switch (buf[0]) {
                                case Contant.FIND_HOST_ADDR_MSG:
                                    InetAddress hostAddr = packet.getAddress();
                                    if (null != connHandler) {
                                        Message msg = new Message();
                                        msg.what = Contant.FIND_HOST_ADDR_MSG;
                                        msg.obj = hostAddr;
                                        connHandler.sendMessage(msg);
                                    }
                                    connectServer(hostAddr);
                                    break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}

package com.youme.server;

import android.os.Handler;
import android.os.Message;

import com.youme.contant.Contant;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * create at 2018年1月8日17:39:49
 */
public class TCPSingleton {
    private int count;//重试次数

    public Handler connHandler;

    InetAddress hostAddr;

    public boolean hasConnect = false;

    private TCPSingleton() {
        receiveBrocast();
        brocastLocalHost();
    }

    private static TCPSingleton singleton = new TCPSingleton();

    public static TCPSingleton getInstance() {
        return singleton;
    }

    //发送广播
    public void brocastLocalHost() {
        new Thread() {
            @Override
            public void run() {
                while (!hasConnect) {
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
//                            break;
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
                                case Contant.RESP_HOST_MSG:
                                    hostAddr = packet.getAddress();
                                    if (null != connHandler) {
                                        Message msg = new Message();
                                        msg.what = Contant.RESP_HOST_MSG;
                                        msg.obj = hostAddr;
                                        connHandler.sendMessage(msg);
                                    }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public static void main(String[] args) throws IOException {
        TCPSingleton t = new TCPSingleton();
        t.brocastLocalHost();
    }
}

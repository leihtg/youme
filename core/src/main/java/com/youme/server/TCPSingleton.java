package com.youme.server;

import android.os.Handler;
import android.os.Message;

import com.youme.contant.Contant;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

/**
 * create at 2018年1月8日17:39:49
 */
public class TCPSingleton {

    public Handler connHandler;

    public boolean hasConnect = false;

    private TCPSingleton() {
        server.start();
        brocastLocalHost();
    }

    private static TCPSingleton singleton = new TCPSingleton();

    public static TCPSingleton getInstance() {
        return singleton;
    }

    public void brocastLocalHost() {
        new Thread() {
            @Override
            public void run() {
                while (!hasConnect) {
                    try {
                        InetAddress address = InetAddress.getByName("255.255.255.255");
                        byte[] hostMsg = new byte[]{Contant.REQ_HOST_MSG};
                        DatagramPacket packet = new DatagramPacket(hostMsg, hostMsg.length, address, Contant.BROCAST_PORT);

                        DatagramSocket ds = new DatagramSocket();
                        ds.setBroadcast(true);
                        ds.send(packet);
                        Thread.sleep(3000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    Thread server = new Thread() {
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
                                InetAddress hostAddr = packet.getAddress();
                                if (null != connHandler) {
                                    Message msg = new Message();
                                    msg.what = Contant.RESP_HOST_MSG;
                                    msg.obj = hostAddr;
                                    connHandler.sendMessage(msg);
                                }
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    public static void main(String[] args) throws IOException {
        TCPSingleton t = new TCPSingleton();
        t.brocastLocalHost();
    }
}

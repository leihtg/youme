package com.core.server;

import android.os.Handler;
import android.os.Message;

import com.anser.contant.Contant;
import com.anser.contant.DataType;
import com.anser.contant.ReceiveData;
import com.anser.model.base.ModelOutBase;
import com.google.gson.Gson;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * create at 2018年1月8日17:39:49
 */
public class TCPSingleton {
    private int count;//重试次数

    private TCPClient tcpClient;

    public Handler connHandler;
    public Handler sendFailHandler;

    //存放handler
    private ConcurrentHashMap<String, Handler> handlerMsg = new ConcurrentHashMap<>();

    //正在查找服务器
    public volatile boolean isfindServerAddr = true;

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

    /**
     * 函数访问
     *
     * @param data
     * @param uuid
     * @param receiveMethod
     * @return
     */
    public boolean FuncSend(String data, String uuid, Handler receiveMethod) {
        try {
            handlerMsg.put(uuid, receiveMethod);
            if (null != tcpClient) {
                if (!tcpClient.isConnected() && !isfindServerAddr) {
                    isfindServerAddr = true;
                    brocastLocalHost();
                }
                return tcpClient.send(DataType.CallFunc, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //发送广播
    public void brocastLocalHost() {
        new Thread() {
            @Override
            public void run() {
                while (isfindServerAddr) {
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
                            isfindServerAddr = false;
                            connectServer(InetAddress.getByName("180.168.28.98"));
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
                                    isfindServerAddr = false;
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

    /**
     * 接收来自服务器的数据,数据中转站,所以的数据通过这个分发出去
     */
    private Handler receiveDataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ReceiveData rd = (ReceiveData) msg.obj;
            switch (rd.type) {
                case DataType.HeartBeat:
                    break;
                case DataType.CallFunc:
                    synchronized (handlerMsg) {
                        ModelOutBase ob = new Gson().fromJson(rd.data, ModelOutBase.class);
                        Handler handler = handlerMsg.get(ob.getUuid());
                        handlerMsg.remove(ob.getUuid());
                        Message callMsg = new Message();
                        callMsg.obj = rd;
                        handler.sendMessage(callMsg);
                    }
                    break;
                case DataType.ClientNotice:
                    break;
            }
        }
    };


}

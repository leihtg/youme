package com.core.server;

import android.os.Handler;
import android.os.Message;

import com.anser.contant.Contant;
import com.anser.contant.ReceiveData;
import com.anser.util.BagPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Thinkpad on 2018/1/14 20:58.
 */
public class TCPClient {
    private Socket client;
    private boolean isConnected = false;
    private InetAddress hostAddr;
    //向UI发送消息
    private Handler connectHandler;
    private Handler receiveDataHandler;
    private Handler sendFailHandler;

    public TCPClient(InetAddress hostAddr, Handler connectHandler, Handler receiveDataHandler, Handler sendFailHandler) {
        this.hostAddr = hostAddr;
        this.connectHandler = connectHandler;
        this.receiveDataHandler = receiveDataHandler;
        this.sendFailHandler = sendFailHandler;
    }

    public void connect() {
        Message msg = new Message();
        try {
            client = new Socket(this.hostAddr, Contant.SERVER_PORT);
            msg.what = Contant.CONN_HOST_SUCC;
            isConnected = true;
        } catch (Exception e) {
            msg.what = Contant.CONN_HOST_FAIL;
            isConnected = false;
            e.printStackTrace();
        } finally {
            if (isConnected) {
                thread_receiveData.start();
                thread_sendData.start();
            } else {
                if (thread_receiveData.isAlive()) {
                    thread_receiveData.interrupt();
                }
                if (thread_sendData.isAlive()) {
                    thread_sendData.interrupt();
                }
                shutDown();
            }
        }
        connectHandler.sendMessage(msg);
    }

    public boolean isConnected() {
        return isConnected;
    }

    /**
     * 关闭client
     */
    public void shutDown() {
        if (null != client) {
            try {
                if (client.isConnected() || !client.isClosed())
                    client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    Thread thread_receiveData = new Thread() {
        @Override
        public void run() {
            try {
                InputStream is = client.getInputStream();
                while (isConnected) {
                    byte[] buf = new byte[BagPacket.getHeadLen()];
                    BagPacket.receiveByLen(is, buf);//读取头信息

                    BagPacket head = BagPacket.splitBag(buf);

                    byte[] bodyBuf = new byte[head.length];
                    BagPacket.receiveByLen(is, bodyBuf);

                    //封装接收到的数据
                    ReceiveData rd = new ReceiveData();
                    rd.dataType = head.type;
                    rd.data = new String(bodyBuf, "UTF8");

                    Message msg = new Message();
                    msg.obj = rd;
                    receiveDataHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                isConnected = false;
                Message msg = new Message();
                msg.what = Contant.CONN_HOST_FAIL;
                connectHandler.sendMessage(msg);
            } finally {
                if (!isConnected) {
                    if (thread_receiveData.isAlive()) {
                        thread_receiveData.interrupt();
                    }
                    shutDown();
                } else {
                    if (thread_receiveData.isInterrupted()) {
                        thread_receiveData.notifyAll();
                    }
                }
            }
        }
    };

    static LinkedBlockingQueue<ReceiveData> queue = new LinkedBlockingQueue<>(3000);

    Thread thread_sendData = new Thread() {
        @Override
        public void run() {
            try {
                while (isConnected) {
                    ReceiveData take = queue.take();
                    if (client.isClosed()) {
                        queue.offer(take);
                        sleep(1000);
                        continue;
                    }
                    synchronized (client) {
                        OutputStream os = client.getOutputStream();
                        byte[] body = take.data.getBytes("UTF8");
                        BagPacket.sendData(os, body, take.dataType);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (null != sendFailHandler) {
                    Message msg = new Message();
                    msg.obj = "fail";
                    sendFailHandler.sendMessage(msg);
                }
            }
        }
    };

    /**
     * 向服务器发送数据
     *
     * @param type
     * @param data
     */
    public boolean send(final int type, final String data) {
        ReceiveData rd = new ReceiveData();
        rd.dataType = type;
        rd.data = data;
        if (!isConnected) {//如果没有连接，只保留最后一个请求数据
            queue.clear();
        }
        return queue.offer(rd);
//        new Thread() {
//            @Override
//            public void run() {
//                synchronized (client) {
//                    try {
//                        byte[] body = data.getBytes("UTF8");
//                        byte[] head = BagPacket.AssembleBag(body.length, type);
//
//                        OutputStream os = client.getOutputStream();
//                        os.write(head);
//                        os.write(body);
//                        os.flush();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        if (null != sendFailHandler) {
//                            Message msg = new Message();
//                            msg.obj = "fail";
//                            sendFailHandler.sendMessage(msg);
//                        }
//                    }
//                }
//            }
//        }.start();
    }

}

package com.youme.server;

import android.os.Handler;
import android.os.Message;

import com.youme.contant.Contant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

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

    //向本线程发送消息
    public Handler recvHandler;

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
            msg.obj = "succ";
            isConnected = true;
        } catch (Exception e) {
            msg.obj = "fail";
            isConnected = false;
            e.printStackTrace();
        } finally {
            if (isConnected) {
                thread_receiveData.start();
            } else {
                if (thread_receiveData.isAlive()) {
                    thread_receiveData.interrupt();
                }
                shutDown();
            }
        }
        connectHandler.sendMessage(msg);
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
                while (isConnected) {
                    byte[] buf = new byte[BagPacket.getHeadLen()];
                    receiveByLen(buf);//读取头信息

                    BagPacket head = BagPacket.splitBag(buf);

                    byte[] bodyBuf = new byte[head.length];
                    receiveByLen(bodyBuf);

                    //封装接收到的数据
                    ReceiveData rd = new ReceiveData();
                    rd.type = head.type;
                    rd.data = new String(bodyBuf, "UTF8");

                    if (null != receiveDataHandler) {
                        Message msg = new Message();
                        msg.obj = rd;
                        receiveDataHandler.sendMessage(msg);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                isConnected = false;
                Message msg = new Message();
                msg.obj = "fail";
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

    /**
     * 向服务器发送数据
     *
     * @param type
     * @param data
     */
    public void send(final int type, final String data) {
        new Thread() {
            @Override
            public void run() {
                synchronized (client) {
                    try {
                        byte[] body = data.getBytes("UTF8");
                        byte[] head = BagPacket.AssembleBag(body.length, type);

                        OutputStream os = client.getOutputStream();
                        os.write(head);
                        os.write(body);
                        os.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Message msg = new Message();
                        msg.obj = "fail";
                        sendFailHandler.sendMessage(msg);
                    }
                }
            }
        }.start();
    }

    /**
     * 读取长度
     *
     * @param bytes
     */
    private void receiveByLen(byte[] bytes) {
        try {
            int len = bytes.length, readLen = 0, r;
            InputStream is = client.getInputStream();
            while (readLen < len) {
                r = is.read(bytes, readLen, len - readLen);
                if (r == -1) {//对方关闭了输出流
                    break;
                }
                readLen += r;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isConnected = false;
            Message msg = new Message();
            msg.obj = "fail";
            connectHandler.sendMessage(msg);
        }
    }

}

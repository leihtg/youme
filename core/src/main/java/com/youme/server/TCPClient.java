package com.youme.server;

import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.youme.contant.Contant;
import com.youme.contant.FileModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

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
    //向本线程发送消息
    public Handler recvHandler;

    static Gson gson = new Gson();

    OutputStream os;

    public TCPClient(InetAddress hostAddr, Handler connectHandler) {
        this.connectHandler = connectHandler;
        this.hostAddr = hostAddr;
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
                receiveData.start();
            } else {
                if (receiveData.isAlive()) {
                    receiveData.interrupt();
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

    Thread receiveData = new Thread() {
        @Override
        public void run() {
            try {
                byte[] buf = new byte[BagPacket.getHeadLen()];
                receiveByLen(buf);//读取头信息

                BagPacket head = BagPacket.splitBag(buf);

                byte[] bodyBuf = new byte[head.length];
                receiveByLen(bodyBuf);

                ReceiveData rd = new ReceiveData();
                rd.type = head.type;
                rd.data = new String(bodyBuf, "UTF8");

                if (null != receiveDataHandler) {
                    Message msg = new Message();
                    msg.obj = rd;
                    receiveDataHandler.sendMessage(msg);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 读取长度
     *
     * @param len
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

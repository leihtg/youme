package com.youme.server;

import android.os.Handler;
import android.os.Looper;
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
public class ClientThread implements Runnable {
    private Socket socket;
    private InetAddress hostAddr;
    //向UI发送消息
    private Handler handler;
    //向本线程发送消息
    public Handler recvHandler;

    static Gson gson = new Gson();

    OutputStream os;

    public ClientThread(Handler handler) {
        this.handler = handler;
        this.hostAddr = TCPSingleton.getInstance().hostAddr;
    }

    @Override
    public void run() {
        try {
            if (null == hostAddr) return;
            socket = new Socket(hostAddr, Contant.SERVER_PORT);

            os = socket.getOutputStream();

            new Thread() {
                @Override
                public void run() {
                    try {
                        InputStream is = socket.getInputStream();
                        byte[] buf = new byte[is.available()];
                        is.read(buf);
                        String json = new String(buf, "UTF8");
                        List<FileModel> list = gson.fromJson(json, new TypeToken<List<FileModel>>() {
                        }.getType());
                        if (null != handler) {
                            Message msg = new Message();
                            msg.what = 0;
                            msg.obj = list;
                            handler.sendMessage(msg);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            //为当前线程初始化Lopper
            Looper.prepare();
            recvHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    try {
                        if (null != os) {
                            os.write(gson.toJson(msg.obj).getBytes("UTF8"));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            //启动loop
            Looper.loop();

            handler.sendEmptyMessage(1);//表明recvHandler初始化成功

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

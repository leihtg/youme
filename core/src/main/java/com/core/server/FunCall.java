package com.core.server;

import android.os.Handler;
import android.os.Message;

import com.anser.contant.*;
import com.anser.model.base.ModelInBase;
import com.anser.model.base.ModelOutBase;
import com.core.constant.MessageType;
import com.google.gson.Gson;

import java.util.UUID;

/**
 * 函数访问
 * Created by leihuating on 2018/1/17.
 */

public class FunCall<IN extends ModelInBase, OUT extends ModelOutBase> {
    private static Gson gson = new Gson();
    public Handler FuncResultHandler;
    private Class<OUT> retClass;//返回数据的类型

    public void call(IN model_in, Class<OUT> retClass) {
        this.retClass = retClass;
        String uuid = UUID.randomUUID().toString();
        model_in.setUuid(uuid);

        String data = gson.toJson(model_in);

        boolean flag = TCPSingleton.getInstance().FuncSend(data, uuid, receiveHandler);
        if (!flag) {
            Message msg = new Message();
            msg.what = MessageType.ERR;
            msg.obj = "发送请求失败";
            receiveHandler.sendMessage(msg);
        }
    }

    private Handler receiveHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ReceiveData rd = (ReceiveData) msg.obj;
            switch (rd.msgType) {
                case MessageType.SUCC:
                    OUT out = gson.fromJson(rd.data, retClass);
                    if (null != FuncResultHandler) {
                        Message sucMsg = new Message();
                        sucMsg.obj = out;
                        sucMsg.what = rd.msgType;
                        FuncResultHandler.sendMessage(sucMsg);
                    }
                    break;
                case MessageType.ERR:
                    if (null != FuncResultHandler) {
                        Message m = new Message();
                        m.copyFrom(msg);
                        FuncResultHandler.sendMessage(m);
                    }
                    break;
            }
        }
    };
}

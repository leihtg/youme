package com.core.server;

import android.os.Handler;
import android.os.Message;

import com.anser.contant.*;
import com.anser.model.base.ModelInBase;
import com.anser.model.base.ModelOutBase;
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
            ModelOutBase out = new ModelOutBase();
            out.setMsg("发送请求失败");
            out.setMsgType(MsgType.ERROR);

            ReceiveData rd = new ReceiveData();
            rd.dataType = DataType.CallFunc;
            rd.data = gson.toJson(out);

            msg.obj = rd;

            receiveHandler.sendMessage(msg);
        }
    }

    private Handler receiveHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ReceiveData rd = (ReceiveData) msg.obj;
            OUT out = gson.fromJson(rd.data, retClass);
            Message resp = new Message();
            resp.obj = out;
            switch (out.msgType) {
                case MsgType.SUCC:
                    if (null != FuncResultHandler) {
                        FuncResultHandler.sendMessage(resp);
                    }
                    break;
                case MsgType.ERROR:
                    if (null != FuncResultHandler) {
                        FuncResultHandler.sendMessage(resp);
                    }
                    break;
            }
        }
    };
}

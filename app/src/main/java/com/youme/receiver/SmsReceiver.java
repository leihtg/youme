package com.youme.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.youme.R;
import com.youme.activity.NotifyActivity;

/**
 * Created by Thinkpad on 2018/1/6.
 */
public class SmsReceiver extends BroadcastReceiver {
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        StringBuilder sb = new StringBuilder();
        Bundle bundle = intent.getExtras();
        switch(intent.getAction()){
            case "TEST_SMS_RECEIVED":
                send(context,"Hello 测试");
                break;
            case ACTION:
                if (null != bundle) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    SmsMessage[] smsMessage = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        smsMessage[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    for (SmsMessage msg : smsMessage) {
                        sb.append("短信来源:");
                        sb.append(msg.getDisplayOriginatingAddress());
                        sb.append("\n-----短信内容----\n");
                        sb.append(msg.getDisplayMessageBody());
                    }
                    Toast.makeText(context, sb.toString(), Toast.LENGTH_LONG).show();
                    send(context, sb.toString());
                }
                break;
        }

    }

    //为发送通知事件的按钮定义事件处理方法
    private void send(Context context, String msg) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //创建一个启动其他Activity的Intent
        Intent intent = new Intent(context, NotifyActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        Notification notify = new Notification.Builder(context)
                //设置打开该通知，该通知自动消失
                .setAutoCancel(true)
                        //设置显示在状态栏的通知提示信息
                .setTicker("有新消息")
                        //设置通知的图标
                .setSmallIcon(R.drawable.cjp)
                        //设置通知内容的标题
                .setContentTitle("一条新短信")
                        //设置通知内容
                .setContentText(msg)
                        //设置使用系统默认的声音,默认LED灯
                .setDefaults(Notification.DEFAULT_SOUND)
                        //| Notification.DEFAULT_LIGHTS)
                        //设置通知自定义声音
                        //.setSound("msg")
                        //设置通知将要启动程序的Intent
                .setContentIntent(pi)
                .build();

        //发送通知
        nm.notify(0, notify);
    }
}

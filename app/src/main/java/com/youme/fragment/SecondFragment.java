package com.youme.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.youme.R;
import com.youme.activity.NotifyActivity;

/**
 * Created by Thinkpad on 2017/2/4.
 */
public class SecondFragment extends Fragment {
    static final int NOTIFICATION_ID = 0x123;
    NotificationManager nm;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.second, null);

        //获取系统的NotificationManager服务
        nm = (NotificationManager) view.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Button send = (Button) view.findViewById(R.id.sendNotify);
        Button cancel = (Button) view.findViewById(R.id.cancleNotify);

        send.setOnClickListener(listen);
        cancel.setOnClickListener(listen);

        return view;
    }

    View.OnClickListener listen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sendNotify:
                    send();
                    break;
                case R.id.cancleNotify:
                    del();
                    break;
            }
        }
    };

    //为发送通知事件的按钮定义事件处理方法
    private void send() {
        //创建一个启动其他Activity的Intent
        Intent intent = new Intent(view.getContext(), NotifyActivity.class);
        PendingIntent pi = PendingIntent.getActivity(view.getContext(), 0, intent, 0);
        Notification notify = new Notification.Builder(view.getContext())
                //设置打开该通知，该通知自动消失
                .setAutoCancel(true)
                        //设置显示在状态栏的通知提示信息
                .setTicker("有新消息")
                        //设置通知的图标
                .setSmallIcon(R.drawable.cjp)
                        //设置通知内容的标题
                .setContentTitle("一条新通知")
                        //设置通知内容
                .setContentText("恭喜您,您加薪了,工资增加100%")
                        //设置使用系统默认的声音,默认LED灯
                .setDefaults(Notification.DEFAULT_SOUND)
                        //| Notification.DEFAULT_LIGHTS)
                        //设置通知自定义声音
                        //.setSound("msg")
                        //设置通知将要启动程序的Intent
                .setContentIntent(pi)
                .build();

        //发送通知
        nm.notify(NOTIFICATION_ID, notify);
    }

    //取消
    private void del() {
        nm.cancel(NOTIFICATION_ID);
    }

}

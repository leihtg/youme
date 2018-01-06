package com.youme.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.youme.R;
import com.youme.service.MyService;

/**
 * Created by Thinkpad on 2017/2/4.
 */
public class SecondFragment extends Fragment {
    private static final String TAG = SecondFragment.class.toString();
    static final int NOTIFICATION_ID = 0x123;
    NotificationManager nm;
    View view;

    Button startService;
    Button unStartService;
    Button bindService;
    Button unBindService;
    Button sendSMS;
    Intent intent = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.second, null);

        //获取系统的NotificationManager服务
        nm = (NotificationManager) view.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Button send = (Button) view.findViewById(R.id.sendNotify);
        Button cancel = (Button) view.findViewById(R.id.cancleNotify);
        startService = (Button) view.findViewById(R.id.startService);
        unStartService = (Button) view.findViewById(R.id.unStartService);
        bindService = (Button) view.findViewById(R.id.bindService);
        unBindService = (Button) view.findViewById(R.id.unBindService);
        sendSMS = (Button) view.findViewById(R.id.sendSMS);


        sendSMS.setOnClickListener(listen);
        startService.setOnClickListener(listen);
        bindService.setOnClickListener(listen);
        unStartService.setOnClickListener(listen);
        unBindService.setOnClickListener(listen);
        send.setOnClickListener(listen);
        cancel.setOnClickListener(listen);
        intent = new Intent(getContext(), MyService.class);
        return view;
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: ");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ");
        }
    };

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
                case R.id.startService:
                    getContext().startService(intent);
                    break;
                case R.id.unStartService:
                    getContext().stopService(intent);
                    break;
                case R.id.bindService:
                    getContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
                    break;
                case R.id.unBindService:
                    getContext().unbindService(conn);
                    break;
                case R.id.sendSMS:
                    getContext().sendBroadcast(new Intent("TEST_SMS_RECEIVED"));
                    break;
            }
        }
    };

    //为发送通知事件的按钮定义事件处理方法
    private void send() {
        //创建一个启动其他Activity的Intent
        Intent intent = new Intent(view.getContext(), Main2Activity.class);
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

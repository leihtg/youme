package com.youme.fragment;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.ViewFlipper;

import com.youme.R;
import com.youme.activity.NotifyActivity;
import com.youme.adapter.FileTransferListAdapter;
import com.youme.db.DbHelper;
import com.youme.entity.FileTransfer;
import com.youme.entity.FileTransferType;
import com.youme.service.FileTransferService;
import com.youme.service.MyService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Thinkpad on 2017/2/4.
 */
public class TransferFragment extends Fragment implements GestureDetector.OnGestureListener {
    private static final String TAG = TransferFragment.class.toString();
    static final int NOTIFICATION_ID = 0x123;
    NotificationManager nm;
    View view;

    Intent intent = null;
    ViewFlipper view_filpper;
    TabHost tabHost;
    FileTransferService.FileBinder binder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(getContext(), FileTransferService.class);
        getContext().bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (FileTransferService.FileBinder) service;
                binder.registerCallback(handlerUpload);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);

        adapter01 = new FileTransferListAdapter(this.getContext(), null);
        adapter02 = new FileTransferListAdapter(this.getContext(), null);
    }

    private List<FileTransfer> list1 = new ArrayList<>();

    private FileTransferListAdapter adapter01;
    private FileTransferListAdapter adapter02;

    @SuppressLint("HandlerLeak")
    private Handler handlerUpload = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;
                default:
                    list1.clear();
                    list1.add((FileTransfer) msg.obj);
                    adapter01.refresh(list1);
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.second, null);

        //获取系统的NotificationManager服务
        nm = (NotificationManager) view.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        intent = new Intent(getContext(), MyService.class);

        tabHost = (TabHost) view.findViewById(R.id.tabhost);
        tabHost.setup();

        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("上传列表").setContent(R.id.tab01));
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("下载列表").setContent(R.id.tab02));
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                switch (tabId) {
                    case "tab1":
                        break;
                    case "tab2":
                        break;
                }
            }
        });
        list01 = (ListView) view.findViewById(R.id.tab01_list);
        list02 = (ListView) view.findViewById(R.id.tab02_list);

        list01.setAdapter(adapter01);
        list02.setAdapter(adapter02);

        return view;
    }

    ListView list01;
    ListView list02;

    private GestureDetector detector;

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getX() - e2.getX() > 120) {
            this.view_filpper.setInAnimation(AnimationUtils.loadAnimation(this.getContext(),
                    R.anim.push_left_in));
            this.view_filpper.setOutAnimation(AnimationUtils.loadAnimation(this.getContext(),
                    R.anim.push_left_out));
            this.view_filpper.showNext();
            return true;
        } else if (e1.getX() - e2.getX() < -120) {
            this.view_filpper.setInAnimation(AnimationUtils.loadAnimation(this.getContext(),
                    R.anim.push_right_in));
            this.view_filpper.setOutAnimation(AnimationUtils.loadAnimation(this.getContext(),
                    R.anim.push_right_out));
            this.view_filpper.showPrevious();
            return true;
        }

        return false;
    }


    @Override
    public boolean onDown(MotionEvent e) {
        System.out.println("e = [" + e + "]");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    FragmentPagerAdapter pagerAdapter = null;

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

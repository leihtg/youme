package com.youme.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.Toast;

import com.youme.R;
import com.youme.fragment.FirstFragment;
import com.youme.fragment.SecondFragment;
import com.youme.fragment.SpeechFragment;

public class MainActivity extends FragmentActivity {
    private FragmentTabHost tabHost;
    private LayoutInflater mInflater;
    private Class[] frags = {FirstFragment.class, SecondFragment.class, SpeechFragment.class};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mInflater = LayoutInflater.from(this);

        tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        int count = frags.length;
        for (int i = 0; i < count; i++) {
            TabHost.TabSpec t1 = tabHost.newTabSpec(String.valueOf(i)).setIndicator(String.valueOf(i));
            tabHost.addTab(t1, frags[i], null);
        }

        RadioGroup mGroup = (RadioGroup) findViewById(R.id.main_group);
        //添加监听
        mGroup.setOnCheckedChangeListener(checkedChangeListener);
        //默认选第一个
        ((RadioButton) mGroup.getChildAt(0)).setChecked(true);
    }

    /**
     * 利用选择指定的radio打开指定的frame
     */
    RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int index = 0;
            switch (checkedId) {
                case R.id.zy:
                    index = 0;
                    break;
                case R.id.trade:
                    index = 1;
                    break;
                case R.id.more:
                    index = 2;
                    break;
                default:
                    return;
            }
            tabHost.setCurrentTab(index);
        }
    };

    /**
     * 重写返回按键事件
     */
    @Override
    public void onBackPressed() {
        exit();
        super.onBackPressed();
    }

    boolean isExit = false;

    private void exit() {
        if (!isExit) {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_LONG).show();
            backHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

    Handler backHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };
}




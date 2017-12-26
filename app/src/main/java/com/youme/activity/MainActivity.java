package com.youme.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.widget.TabHost;

import com.youme.R;
import com.youme.fragment.FirstFragment;
import com.youme.fragment.SecondFragment;
import com.youme.fragment.SpeechFragment;

public class MainActivity extends FragmentActivity {
    private FragmentTabHost tabHost;
    private LayoutInflater mInflater;
    private Class[] frags = {FirstFragment.class, SecondFragment.class, SpeechFragment.class};
    private int[] imgs = {R.drawable.yd, R.drawable.lt};
    private String[] btnName = {"移动", "联通", "听书"};

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
            TabHost.TabSpec t1 = tabHost.newTabSpec(btnName[i]).setIndicator(btnName[i]);
            tabHost.addTab(t1, frags[i], null);
        }
    }

}












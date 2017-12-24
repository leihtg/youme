package com.youme.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.leihtg.youme.FirstFragment;
import com.leihtg.youme.SecondFragment;
import com.leihtg.youme.SpeechFragment;
import com.youme.R;

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


    private View getTabItemView(int index) {
        View view = mInflater.inflate(R.layout.tab_item_view, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
        if (index < imgs.length) {
            imageView.setImageResource(imgs[index]);
        }
        TextView textView = (TextView) view.findViewById(R.id.textview);
        textView.setText(btnName[index]);

        return view;
    }

}












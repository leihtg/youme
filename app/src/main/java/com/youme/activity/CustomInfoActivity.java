package com.youme.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.youme.R;

/**
 * Create at 2018年1月6日22:34:47
 */
public class CustomInfoActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_info);
        back = (ImageView) findViewById(R.id.custInfo_back);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.custInfo_back:
                super.onBackPressed();
                break;
        }
    }
}

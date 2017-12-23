package com.leihtg.youme;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.youme.R;

import java.util.HashMap;

public class FirstFragment extends Fragment {
    private static final String PHONE = "yd";
    private View view;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText phone = (EditText) view.findViewById(R.id.yd_phone);
            EditText pwd = (EditText) view.findViewById(R.id.yd_pwd);
            Toast.makeText(getActivity(),"手机号："+phone.getText()+",服务密码："+pwd.getText(),Toast.LENGTH_LONG).show();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.first, null);
        if (savedInstanceState != null) {
            Object o = savedInstanceState.get(PHONE);
            if (o != null) {
                EditText phone = (EditText) view.findViewById(R.id.yd_phone);
                EditText pwd = (EditText) view.findViewById(R.id.yd_pwd);
                HashMap<String, String> map = (HashMap<String, String>) o;
                phone.setText(map.get("phone"));
                pwd.setText(map.get("pwd"));
            }
        }
        Button searchBtn = (Button) view.findViewById(R.id.yd_searchBtn);
        searchBtn.setOnClickListener(clickListener);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (view != null) {
            EditText phone = (EditText) view.findViewById(R.id.yd_phone);
            EditText pwd = (EditText) view.findViewById(R.id.yd_pwd);
            HashMap<String, String> map = new HashMap<>();
            map.put("phone", phone.getText().toString());
            map.put("pwd", pwd.getText().toString());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}

package com.youme.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.youme.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirstFragment extends Fragment {
    private View view;

    private String[] names = new String[]{"虎头", "弄玉", "李清照", "李白"};
    private String[] descs = new String[]{"可爱的小孩", "一个擅长音乐的女孩", "一个擅长文学的女性", "浪漫主义诗人"};
    private int[] imageIds = new int[]{R.drawable.chenp, R.drawable.lt, R.drawable.cjp, R.drawable.home_01};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.first, null);
        List<Map<String, Object>> listItems = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> listItem = new HashMap<>();
            listItem.put("header", imageIds[i]);
            listItem.put("personName", names[i]);
            listItem.put("desc", descs[i]);
            listItems.add(listItem);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(view.getContext(), listItems, R.layout.dir_item, new String[]{"personName", "header", "desc"}, new int[]{R.id.name, R.id.header, R.id.desc});
        ListView listView = (ListView) view.findViewById(R.id.dirList);
        //为ListView设置Adapter
        listView.setAdapter(simpleAdapter);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}

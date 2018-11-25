package com.youme.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.anser.model.FileModel;
import com.youme.R;
import com.youme.util.FileUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 填充文件列表的adapter
 * Created by leihuating on 2018/1/17.
 */

public class FileListAdapter extends BaseAdapter {
    Context context;
    List<FileModel> list;
    boolean canCheck = false;


    public FileListAdapter(Context context, List<FileModel> list) {
        this(context, list, false);
    }

    public FileListAdapter(Context context, boolean canCheck) {
        this(context, null, canCheck);
    }

    public FileListAdapter(Context context, List<FileModel> list, boolean canCheck) {
        this.context = context;
        this.list = list;
        this.canCheck = canCheck;
    }

    @Override
    public int getCount() {
        return null == list ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (null == view) {
            view = LayoutInflater.from(context).inflate(R.layout.file_list, parent, false);
        }
        if (null == list || list.isEmpty()) {
            return view;
        }
        ImageView fileIcon = (ImageView) view.findViewById(R.id.fileIcon);
        TextView createTime = (TextView) view.findViewById(R.id.createTime);
        TextView fileSize = (TextView) view.findViewById(R.id.fileSize);
        TextView fileName = (TextView) view.findViewById(R.id.fileName);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.file_check);
        checkBox.setVisibility(canCheck ? View.VISIBLE : View.INVISIBLE);

        final FileModel fm = list.get(position);

        view.setTag(fm.getPath());

        fileSize.setText(FileUtil.getSize(fm.getLength()));
        createTime.setText(FileUtil.formatTime(fm.getLastModified()));
        fileName.setText(fm.getName());

        if (fm.isDir()) {
            fileIcon.setImageResource(R.mipmap.folder);
            fileSize.setVisibility(View.GONE);
        } else {//按文件格式显示图标
            fileSize.setVisibility(View.VISIBLE);
            fileIcon.setImageResource(FileUtil.getImg(fm.getName()));
        }
        if (canCheck) {
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    boolean contains = map.contains(fm.getPath());
                    if (isChecked) {
                        if (!contains)
                            map.add(fm.getPath());
                    } else {
                        map.remove(fm.getPath());
                    }
                }
            });
            if (map.contains(fm.getPath())) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
        }

        return view;
    }

    /*存放已经选择的号*/
    private List<String> map = new ArrayList<>();

    public Collection<String> getSelectedFiles() {
        return map;
    }

    public void setSelectedFiles(List<String> list) {
        if (null != list) {
            map = list;
        }
    }


    /**
     * 刷新数据
     *
     * @param list
     */
    public void refresh(List<FileModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

}

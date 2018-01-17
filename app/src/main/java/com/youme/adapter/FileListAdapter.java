package com.youme.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.anser.model.FileModel;
import com.youme.R;
import com.youme.util.FileUtil;

import java.util.List;

/**
 * 填充文件列表的adapter
 * Created by leihuating on 2018/1/17.
 */

public class FileListAdapter extends BaseAdapter {
    Context context;
    List<FileModel> list;


    public FileListAdapter(Context context, List<FileModel> list) {
        this.context = context;
        this.list = list;
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
    public View getView(int position, View view, ViewGroup parent) {
        if (null == view) {
            view = LayoutInflater.from(context).inflate(R.layout.file_list, parent, false);
        }
        if (null == list || list.isEmpty()) {
            return view;
        }
        ImageView fileIcon = (ImageView) view.findViewById(R.id.fileIcon);
        TextView createTime = (TextView) view.findViewById(R.id.createTime);
        TextView fileSize = (TextView) view.findViewById(R.id.fileSize);

        FileModel fm = list.get(position);
        fileSize.setText(FileUtil.getSize(fm.getLength()));
        createTime.setText(FileUtil.formatTime(fm.getLastModified()));
        if (fm.isDir()) {
            fileIcon.setImageResource(R.mipmap.folder);
        } else {//按文件格式显示图标
            fileIcon.setImageResource(FileUtil.getImg(fm.getName()));
        }

        return view;
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
